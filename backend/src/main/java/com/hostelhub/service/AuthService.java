package com.hostelhub.service;

import com.hostelhub.dto.AuthResponse;
import com.hostelhub.dto.LoginRequest;
import com.hostelhub.dto.StudentDTO;
import com.hostelhub.dto.StudentRegisterRequest;
import com.hostelhub.entity.Student;
import com.hostelhub.entity.User;
import com.hostelhub.entity.Warden;
import com.hostelhub.exception.BadRequestException;
import com.hostelhub.repository.StudentRepository;
import com.hostelhub.repository.UserRepository;
import com.hostelhub.repository.WardenRepository;
import com.hostelhub.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WardenRepository wardenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest loginRequest) {
        String identifier = loginRequest.getIdentifier().trim();
        String requestedRole = loginRequest.getRole().trim().toUpperCase();

        // 1. Resolve username from email or student/warden ID
        String actualUsername = identifier;
        Optional<User> userOpt = userRepository.findByUsername(identifier.toLowerCase());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        // Domain typo tolerance fallback (e.g. gamil.com <-> gmail.com)
        if (userOpt.isEmpty() && identifier.contains("@")) {
            String altEmail = null;
            if (identifier.toLowerCase().endsWith("@gamil.com")) {
                altEmail = identifier.toLowerCase().replace("@gamil.com", "@gmail.com");
            } else if (identifier.toLowerCase().endsWith("@gmail.com")) {
                altEmail = identifier.toLowerCase().replace("@gmail.com", "@gamil.com");
            }
            if (altEmail != null) {
                userOpt = userRepository.findByUsername(altEmail);
            }
        }

        if (userOpt.isEmpty()) {
            // Check student email or ID
            Optional<Student> studentOpt = studentRepository.findById(identifier.toUpperCase());
            if (studentOpt.isEmpty()) {
                studentOpt = studentRepository.findById(identifier);
            }
            if (studentOpt.isEmpty()) {
                studentOpt = studentRepository.findByEmail(identifier.toLowerCase());
            }
            if (studentOpt.isEmpty()) {
                studentOpt = studentRepository.findByEmail(identifier);
            }

            if (studentOpt.isPresent() && studentOpt.get().getUser() != null) {
                actualUsername = studentOpt.get().getUser().getUsername();
            } else {
                // Check warden email or ID
                Optional<Warden> wardenOpt = wardenRepository.findById(identifier.toUpperCase());
                if (wardenOpt.isEmpty()) {
                    wardenOpt = wardenRepository.findById(identifier);
                }
                if (wardenOpt.isEmpty()) {
                    wardenOpt = wardenRepository.findByEmail(identifier.toLowerCase());
                }
                if (wardenOpt.isEmpty()) {
                    wardenOpt = wardenRepository.findByEmail(identifier);
                }
                if (wardenOpt.isPresent() && wardenOpt.get().getUser() != null) {
                    actualUsername = wardenOpt.get().getUser().getUsername();
                }
            }
        } else {
            actualUsername = userOpt.get().getUsername();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(actualUsername, loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(actualUsername)
                .orElseThrow(() -> new BadRequestException("User profile not found"));

        if (!user.getRole().name().equalsIgnoreCase(requestedRole)) {
            throw new BadRequestException("Invalid role selected for user account! Target account role is " + user.getRole().name());
        }

        String name = user.getUsername();
        String studentId = null;
        String wardenId = null;

        if (user.getRole() == User.Role.STUDENT) {
            Optional<Student> student = studentRepository.findByUser(user);
            if (student.isEmpty()) {
                student = studentRepository.findByEmail(user.getUsername());
            }
            if (student.isPresent()) {
                name = student.get().getFullName();
                studentId = student.get().getId();
            }
        } else if (user.getRole() == User.Role.WARDEN) {
            Optional<Warden> warden = wardenRepository.findByUser(user);
            if (warden.isEmpty()) {
                warden = wardenRepository.findByEmail(user.getUsername());
            }
            if (warden.isPresent()) {
                name = warden.get().getName();
                wardenId = warden.get().getId();
            }
        } else if (user.getRole() == User.Role.ADMIN) {
            name = "System Administrator";
        }

        return new AuthResponse(jwt, user.getRole().name(), user.getId(), name, studentId, wardenId);
    }

    @Transactional
    public StudentDTO registerStudent(StudentRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (email.endsWith("@gamil.com")) {
            email = email.replace("@gamil.com", "@gmail.com");
        }

        if (studentRepository.existsByEmail(email) || studentRepository.existsByEmail(request.getEmail().trim())) {
            throw new BadRequestException("Email is already registered!");
        }

        if (userRepository.existsByUsername(email) || userRepository.existsByUsername(request.getEmail().trim())) {
            throw new BadRequestException("User account with email already exists!");
        }

        String password = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "student123";

        // Create User account
        User user = new User(email, passwordEncoder.encode(password), User.Role.STUDENT);
        user = userRepository.save(user);

        // Generate non-colliding student ID: STU-XXXX
        long maxId = 1000;
        for (Student s : studentRepository.findAll()) {
            if (s.getId() != null && s.getId().toUpperCase().startsWith("STU-")) {
                try {
                    long num = Long.parseLong(s.getId().substring(4));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        String studentId = String.format("STU-%d", maxId + 1);

        String phone = request.getPhone() != null ? request.getPhone().replaceAll("[^0-9]", "") : "";

        Student student = new Student(
                studentId,
                request.getFullName().trim(),
                email,
                phone,
                request.getDepartment().trim(),
                request.getYear().trim(),
                request.getGender().trim(),
                request.getEmergencyContact().trim(),
                request.getRoomNeeds() != null ? request.getRoomNeeds().trim() : ""
        );
        student.setUser(user);
        student = studentRepository.save(student);

        return mapToStudentDTO(student);
    }

    public StudentDTO mapToStudentDTO(Student s) {
        StudentDTO dto = new StudentDTO();
        dto.setId(s.getId());
        dto.setName(s.getFullName());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setDept(s.getDepartment());
        dto.setYear(s.getYear());
        dto.setGender(s.getGender());
        dto.setEmergencyContact(s.getEmergencyContact());
        dto.setNeeds(s.getRoomNeeds());
        dto.setAssignedRoom(s.getAssignedRoom() != null ? s.getAssignedRoom().getId() : "Unassigned");
        dto.setStatus(s.getAllocationStatus());
        return dto;
    }
}
