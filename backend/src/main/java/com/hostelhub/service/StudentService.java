package com.hostelhub.service;

import com.hostelhub.dto.RoomDTO;
import com.hostelhub.dto.StudentDTO;
import com.hostelhub.entity.Room;
import com.hostelhub.entity.Student;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.RoomRepository;
import com.hostelhub.repository.StudentRepository;
import com.hostelhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private com.hostelhub.repository.ComplaintRepository complaintRepository;

    @Autowired
    private com.hostelhub.repository.LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private com.hostelhub.repository.GateLogRepository gateLogRepository;

    @Autowired
    private com.hostelhub.repository.WardenRepository wardenRepository;

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<StudentDTO> getStudentsForUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) return getAllStudents();

        boolean isStudent = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        boolean isWarden = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WARDEN"));

        if (isStudent) {
            String username = authentication.getName();
            StudentDTO me = getStudentByUsername(username);
            return List.of(me);
        }

        if (isWarden) {
            String username = authentication.getName();
            com.hostelhub.entity.User user = userRepository.findByUsername(username).orElse(null);
            com.hostelhub.entity.Warden warden = null;
            if (user != null) {
                warden = wardenRepository.findByUser(user).orElse(null);
            }
            if (warden == null) {
                warden = wardenRepository.findByEmail(username).orElse(null);
            }

            if (warden != null && warden.getAssignedBlock() != null) {
                final String block = warden.getAssignedBlock().trim();
                return studentRepository.findAll().stream()
                        .filter(s -> (s.getAssignedRoom() != null && block.equalsIgnoreCase(s.getAssignedRoom().getBlock()))
                                || s.getAssignedRoom() == null
                                || "Pending Allocation".equalsIgnoreCase(s.getAllocationStatus()))
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
            }
        }

        return getAllStudents();
    }

    public StudentDTO getStudentByUsername(String username) {
        com.hostelhub.entity.User user = userRepository.findByUsername(username.toLowerCase())
                .orElseGet(() -> userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username)));

        Student student = studentRepository.findByUser(user)
                .orElseGet(() -> studentRepository.findByEmail(username.toLowerCase())
                .orElseGet(() -> studentRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + username))));

        return mapToDTO(student);
    }

    @Transactional
    public StudentDTO updateStudentByUsername(String username, StudentDTO dto) {
        StudentDTO me = getStudentByUsername(username);
        return updateStudent(me.getId(), dto);
    }

    public StudentDTO getStudentById(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return mapToDTO(student);
    }

    @Transactional
    public StudentDTO updateStudent(String id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) student.setFullName(dto.getName().trim());
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) student.setPhone(dto.getPhone().replaceAll("[^0-9]", ""));
        if (dto.getDept() != null && !dto.getDept().isBlank()) student.setDepartment(dto.getDept().trim());
        if (dto.getYear() != null && !dto.getYear().isBlank()) student.setYear(dto.getYear().trim());
        if (dto.getGender() != null && !dto.getGender().isBlank()) student.setGender(dto.getGender().trim());
        if (dto.getEmergencyContact() != null && !dto.getEmergencyContact().isBlank()) student.setEmergencyContact(dto.getEmergencyContact().trim());
        if (dto.getNeeds() != null) student.setRoomNeeds(dto.getNeeds().trim());

        student = studentRepository.save(student);
        return mapToDTO(student);
    }

    @Transactional
    public void deleteStudent(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        // Vacate room first if assigned
        if (student.getAssignedRoom() != null) {
            vacateStudent(id);
        }

        // Clean up complaints, leaves, gate logs for student
        complaintRepository.deleteAll(complaintRepository.findByStudentId(id));
        leaveRequestRepository.deleteAll(leaveRequestRepository.findByStudentId(id));
        gateLogRepository.deleteAll(gateLogRepository.findByStudentIdOrderByScanTimeDesc(id));

        com.hostelhub.entity.User user = student.getUser();
        studentRepository.delete(student);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public StudentDTO vacateStudent(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        Room room = student.getAssignedRoom();
        if (room != null) {
            student.setAssignedRoom(null);
            student.setAllocationStatus("Pending Allocation");
            student = studentRepository.save(student);

            // Update room occupancy
            List<Student> occupants = studentRepository.findByAssignedRoomId(room.getId());
            room.setCurrentOccupancy(occupants.size());
            room.updateStatusBasedOnOccupancy();
            roomRepository.save(room);
        }

        return mapToDTO(student);
    }

    public RoomDTO getStudentRoom(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (student.getAssignedRoom() == null) {
            throw new ResourceNotFoundException("Student has no assigned room");
        }

        return roomService.getRoomById(student.getAssignedRoom().getId());
    }

    public StudentDTO mapToDTO(Student s) {
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
