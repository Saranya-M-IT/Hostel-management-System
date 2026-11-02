package com.hostelhub.config;

import com.hostelhub.entity.*;
import com.hostelhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private WardenRepository wardenRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private HostelRuleRepository hostelRuleRepository;

    @Autowired
    private HostelQRCodeRepository qrCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 0. Ensure single common hostel QR code token exists
        if (qrCodeRepository.count() == 0) {
            qrCodeRepository.save(new HostelQRCode("HOSTEL-HUB-COMMON-GATE-QR-2026", "ACTIVE"));
        }

        // 1. Ensure System Administrator accounts exist with BCrypt password
        createOrUpdateUser("admin123@gmail.com", "admin123", User.Role.ADMIN);
        createOrUpdateUser("admin@hms.edu", "admin123", User.Role.ADMIN);
        createOrUpdateUser("admin", "admin123", User.Role.ADMIN);

        // 2. Create Base Hostel Rooms if table is empty
        if (roomRepository.count() == 0) {
            List<Room> rooms = Arrays.asList(
                    new Room("A-101", "Block A", "101", 1, "Double AC", 2),
                    new Room("A-102", "Block A", "102", 1, "Double Non-AC", 2),
                    new Room("A-103", "Block A", "103", 1, "Single AC", 1),
                    new Room("A-201", "Block A", "201", 2, "Double AC", 2),
                    new Room("A-202", "Block A", "202", 2, "Single Deluxe", 1),
                    new Room("A-203", "Block A", "203", 2, "Double Non-AC", 2),
                    new Room("B-101", "Block B", "101", 1, "Double AC", 2),
                    new Room("B-102", "Block B", "102", 1, "Single AC", 1),
                    new Room("B-201", "Block B", "201", 2, "Double Non-AC", 2),
                    new Room("B-202", "Block B", "202", 2, "Double AC", 2),
                    new Room("B-301", "Block B", "301", 3, "Single Deluxe", 1),
                    new Room("C-101", "Block C", "101", 1, "Double AC", 2),
                    new Room("C-102", "Block C", "102", 1, "Double Non-AC", 2),
                    new Room("C-201", "Block C", "201", 2, "Single AC", 1)
            );
            roomRepository.saveAll(rooms);
        }

        // 3. Remove any remaining demo accounts to ensure clean database
        cleanDemoData();

        // 4. Update room occupancies from genuine database records
        updateRoomOccupancies();

        // 5. Create Hostel Rules if empty
        if (hostelRuleRepository.count() == 0) {
            List<HostelRule> rules = Arrays.asList(
                    new HostelRule("RUL-1", "Night Curfew & Entry Timings", "Timings", "Hostel gates close strictly at 10:00 PM. Late entry requires prior permission letter approved by Block Warden.", true),
                    new HostelRule("RUL-2", "Guest & Visitor Policy", "Security", "Visitors permitted in the hostel reception lounge only between 4:00 PM and 7:00 PM. Overnight guests in rooms strictly prohibited.", true),
                    new HostelRule("RUL-3", "Prohibited Electrical Items", "Maintenance", "High-wattage appliances (heaters, electric kettles, hot plates) are banned to avoid electrical overloads.", true),
                    new HostelRule("RUL-4", "Mess Schedule & Dining Discipline", "Dining", "Breakfast: 7:30 - 9:30 AM | Lunch: 12:30 - 2:30 PM | Dinner: 7:30 - 9:30 PM. Food cannot be taken outside mess without Warden sick pass.", true),
                    new HostelRule("RUL-5", "Quiet Study Hours", "General Discipline", "Strict quiet hours enforced in all corridors and rooms from 11:00 PM to 6:00 AM daily.", true)
            );
            hostelRuleRepository.saveAll(rules);
        }
    }

    private void cleanDemoData() {
        List<String> demoStudentIds = Arrays.asList("STU-1001", "STU-1002", "STU-1003", "STU-1004", "STU-1005", "STU-1006", "STU-1007");
        for (String id : demoStudentIds) {
            if (studentRepository.existsById(id)) {
                complaintRepository.deleteAll(complaintRepository.findByStudentId(id));
                leaveRequestRepository.deleteAll(leaveRequestRepository.findByStudentId(id));
                Student s = studentRepository.findById(id).orElse(null);
                if (s != null) {
                    User u = s.getUser();
                    studentRepository.delete(s);
                    if (u != null) userRepository.delete(u);
                }
            }
        }

        List<String> demoWardenIds = Arrays.asList("WRD-201", "WRD-202", "WRD-203");
        for (String id : demoWardenIds) {
            if (wardenRepository.existsById(id)) {
                Warden w = wardenRepository.findById(id).orElse(null);
                if (w != null) {
                    User u = w.getUser();
                    wardenRepository.delete(w);
                    if (u != null) userRepository.delete(u);
                }
            }
        }

        List<String> demoUsernames = Arrays.asList(
                "alex.m@student.edu", "ethan.h@student.edu", "sophia.c@student.edu",
                "marcus.r@student.edu", "emily.w@student.edu", "liam.n@student.edu",
                "olivia.m@student.edu", "robert.vance@hms.edu", "sarah.jenkins@hms.edu",
                "david.miller@hms.edu"
        );
        for (String username : demoUsernames) {
            userRepository.findByUsername(username).ifPresent(userRepository::delete);
        }
    }

    private User createOrUpdateUser(String username, String rawPassword, User.Role role) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = new User(username, passwordEncoder.encode(rawPassword), role);
        } else {
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setEnabled(true);
        }
        return userRepository.save(user);
    }

    private void updateRoomOccupancies() {
        for (Room room : roomRepository.findAll()) {
            List<Student> occupants = studentRepository.findByAssignedRoomId(room.getId());
            room.setCurrentOccupancy(occupants.size());
            room.updateStatusBasedOnOccupancy();
            roomRepository.save(room);
        }
    }
}
