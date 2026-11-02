package com.hostelhub.service;

import com.hostelhub.dto.AdminDashboardDTO;
import com.hostelhub.dto.StudentDashboardDTO;
import com.hostelhub.dto.WardenDashboardDTO;
import com.hostelhub.entity.Complaint;
import com.hostelhub.entity.LeaveRequest;
import com.hostelhub.entity.Room;
import com.hostelhub.entity.Student;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WardenRepository wardenRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    public AdminDashboardDTO getAdminDashboard() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        List<Room> allRooms = roomRepository.findAll();
        long totalRooms = allRooms.size();
        long occupiedRooms = allRooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.FULL || r.getCurrentOccupancy() >= r.getCapacity()).count();
        long vacantRooms = totalRooms - occupiedRooms;
        long totalCapacity = allRooms.stream().mapToLong(Room::getCapacity).sum();

        List<Student> allStudents = studentRepository.findAll();
        long totalStudents = allStudents.size();
        long allocatedStudents = allStudents.stream().filter(s -> s.getAssignedRoom() != null).count();
        long unallocatedStudents = totalStudents - allocatedStudents;

        long pendingLeaves = leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING).size();
        long pendingComplaints = complaintRepository.findByStatus(Complaint.ComplaintStatus.PENDING).size();

        int occupancyRate = totalRooms > 0 ? (int) Math.round(((double) occupiedRooms / totalRooms) * 100) : 0;

        dto.setTotalStudents(totalStudents);
        dto.setTotalWardens(wardenRepository.count());
        dto.setTotalRooms(totalRooms);
        dto.setOccupiedRooms(occupiedRooms);
        dto.setVacantRooms(vacantRooms);
        dto.setTotalCapacity(totalCapacity);
        dto.setAllocatedStudents(allocatedStudents);
        dto.setUnallocatedStudents(unallocatedStudents);
        dto.setPendingLeaves(pendingLeaves);
        dto.setPendingComplaints(pendingComplaints);
        dto.setOccupancyRate(occupancyRate);

        return dto;
    }

    @Autowired
    private com.hostelhub.repository.UserRepository userRepository;

    public WardenDashboardDTO getWardenDashboard(String wardenUsernameOrId) {
        WardenDashboardDTO dto = new WardenDashboardDTO();

        com.hostelhub.entity.Warden warden = null;
        if (wardenUsernameOrId != null && !wardenUsernameOrId.isBlank()) {
            warden = wardenRepository.findById(wardenUsernameOrId).orElse(null);
            if (warden == null) {
                com.hostelhub.entity.User user = userRepository.findByUsername(wardenUsernameOrId).orElse(null);
                if (user != null) {
                    warden = wardenRepository.findByUser(user).orElse(null);
                }
            }
            if (warden == null) {
                warden = wardenRepository.findByEmail(wardenUsernameOrId).orElse(null);
            }
        }

        if (warden != null && warden.getAssignedBlock() != null) {
            final String block = warden.getAssignedBlock().trim();
            long pendingLeaves = leaveRequestRepository.findAll().stream()
                    .filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.PENDING
                            && l.getStudent() != null && l.getStudent().getAssignedRoom() != null
                            && block.equalsIgnoreCase(l.getStudent().getAssignedRoom().getBlock()))
                    .count();

            long pendingComplaints = complaintRepository.findAll().stream()
                    .filter(c -> c.getStatus() == Complaint.ComplaintStatus.PENDING
                            && c.getStudent() != null && c.getStudent().getAssignedRoom() != null
                            && block.equalsIgnoreCase(c.getStudent().getAssignedRoom().getBlock()))
                    .count();

            long unallocatedStudents = studentRepository.findByAllocationStatus("Pending Allocation").size();
            long vacantRooms = roomRepository.findAll().stream()
                    .filter(r -> block.equalsIgnoreCase(r.getBlock())
                            && r.getStatus() != Room.RoomStatus.FULL
                            && r.getCurrentOccupancy() < r.getCapacity())
                    .count();

            dto.setPendingLeaves(pendingLeaves);
            dto.setPendingComplaints(pendingComplaints);
            dto.setUnallocatedStudents(unallocatedStudents);
            dto.setVacantRooms(vacantRooms);
            return dto;
        }

        long pendingLeaves = leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING).size();
        long pendingComplaints = complaintRepository.findByStatus(Complaint.ComplaintStatus.PENDING).size();
        long unallocatedStudents = studentRepository.findByAllocationStatus("Pending Allocation").size();
        long vacantRooms = roomRepository.findAll().stream()
                .filter(r -> r.getStatus() != Room.RoomStatus.FULL && r.getCurrentOccupancy() < r.getCapacity())
                .count();

        dto.setPendingLeaves(pendingLeaves);
        dto.setPendingComplaints(pendingComplaints);
        dto.setUnallocatedStudents(unallocatedStudents);
        dto.setVacantRooms(vacantRooms);

        return dto;
    }

    public WardenDashboardDTO getWardenDashboard() {
        return getWardenDashboard(null);
    }

    public StudentDashboardDTO getStudentDashboardForUsername(String username) {
        com.hostelhub.entity.User user = userRepository.findByUsername(username.toLowerCase())
                .orElseGet(() -> userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username)));

        Student student = studentRepository.findByUser(user)
                .orElseGet(() -> studentRepository.findByEmail(username.toLowerCase())
                .orElseGet(() -> studentRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + username))));

        return getStudentDashboard(student.getId());
    }

    public StudentDashboardDTO getStudentDashboard(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        StudentDashboardDTO dto = new StudentDashboardDTO();
        dto.setStudentId(student.getId());
        dto.setName(student.getFullName());
        dto.setAssignedRoom(student.getAssignedRoom() != null ? student.getAssignedRoom().getId() : "Unassigned");

        long studentPendingLeaves = leaveRequestRepository.findByStudentId(studentId).stream()
                .filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.PENDING)
                .count();

        long studentPendingComplaints = complaintRepository.findByStudentId(studentId).stream()
                .filter(c -> c.getStatus() == Complaint.ComplaintStatus.PENDING)
                .count();

        dto.setPendingLeaves(studentPendingLeaves);
        dto.setPendingComplaints(studentPendingComplaints);

        return dto;
    }
}
