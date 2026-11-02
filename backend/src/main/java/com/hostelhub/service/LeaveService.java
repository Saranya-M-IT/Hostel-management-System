package com.hostelhub.service;

import com.hostelhub.dto.LeaveActionRequest;
import com.hostelhub.dto.LeaveRequestDTO;
import com.hostelhub.entity.LeaveRequest;
import com.hostelhub.entity.Student;
import com.hostelhub.exception.BadRequestException;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.LeaveRequestRepository;
import com.hostelhub.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.hostelhub.repository.UserRepository userRepository;

    @Autowired
    private com.hostelhub.repository.WardenRepository wardenRepository;

    public List<LeaveRequestDTO> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> getLeaveRequestsForUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) return getAllLeaveRequests();

        boolean isStudent = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        boolean isWarden = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WARDEN"));

        if (isStudent) {
            String username = authentication.getName();
            com.hostelhub.entity.User user = userRepository.findByUsername(username).orElse(null);
            Student student = null;
            if (user != null) {
                student = studentRepository.findByUser(user).orElse(null);
            }
            if (student == null) {
                student = studentRepository.findByEmail(username).orElse(null);
            }
            if (student != null) {
                return getLeaveRequestsByStudent(student.getId());
            }
            return List.of();
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
                return leaveRequestRepository.findAll().stream()
                        .filter(l -> l.getStudent() == null || l.getStudent().getAssignedRoom() == null
                                || block.equalsIgnoreCase(l.getStudent().getAssignedRoom().getBlock()))
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
            }
        }

        return getAllLeaveRequests();
    }

    public List<LeaveRequestDTO> getLeaveRequestsByStudent(String studentId) {
        return leaveRequestRepository.findByStudentId(studentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestDTO submitLeaveRequest(LeaveRequestDTO dto, String authenticatedUsername) {
        Student student = null;
        if (dto.getStudentId() != null && !dto.getStudentId().isBlank()) {
            student = studentRepository.findById(dto.getStudentId()).orElse(null);
        }
        if (student == null && authenticatedUsername != null) {
            com.hostelhub.entity.User user = userRepository.findByUsername(authenticatedUsername).orElse(null);
            if (user != null) {
                student = studentRepository.findByUser(user).orElse(null);
            }
            if (student == null) {
                student = studentRepository.findByEmail(authenticatedUsername).orElse(null);
            }
        }

        if (student == null) {
            throw new BadRequestException("Valid student ID is required for leave request");
        }

        try {
            LocalDate start = LocalDate.parse(dto.getStartDate());
            LocalDate end = LocalDate.parse(dto.getEndDate());
            if (start.isBefore(LocalDate.now())) {
                throw new BadRequestException("Leave start date cannot be in the past!");
            }
            if (end.isBefore(start)) {
                throw new BadRequestException("Leave end date cannot be earlier than start date!");
            }
        } catch (Exception ex) {
            if (ex instanceof BadRequestException) throw ex;
            if (dto.getStartDate().compareTo(dto.getEndDate()) > 0) {
                throw new BadRequestException("Leave end date cannot be earlier than start date!");
            }
        }

        long maxId = 800;
        for (LeaveRequest l : leaveRequestRepository.findAll()) {
            if (l.getId() != null && l.getId().toUpperCase().startsWith("REQ-")) {
                try {
                    long num = Long.parseLong(l.getId().substring(4));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        String reqId = String.format("REQ-%d", maxId + 1);

        LeaveRequest request = new LeaveRequest(
                reqId,
                student,
                dto.getLeaveType(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getParentContact() != null ? dto.getParentContact() : student.getEmergencyContact(),
                dto.getReason()
        );

        request = leaveRequestRepository.save(request);
        return mapToDTO(request);
    }

    @Transactional
    public LeaveRequestDTO submitLeaveRequest(LeaveRequestDTO dto) {
        return submitLeaveRequest(dto, null);
    }

    @Transactional
    public LeaveRequestDTO approveLeaveRequest(String id, LeaveActionRequest actionRequest) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (actionRequest == null || actionRequest.getWardenRemarks() == null || actionRequest.getWardenRemarks().trim().isEmpty()) {
            throw new BadRequestException("Warden remarks are mandatory for approving leave requests.");
        }

        request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        request.setWardenRemarks(actionRequest.getWardenRemarks().trim());

        request = leaveRequestRepository.save(request);
        return mapToDTO(request);
    }

    @Transactional
    public LeaveRequestDTO rejectLeaveRequest(String id, LeaveActionRequest actionRequest) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (actionRequest == null || actionRequest.getWardenRemarks() == null || actionRequest.getWardenRemarks().trim().isEmpty()) {
            throw new BadRequestException("Warden remarks are mandatory for rejecting leave requests.");
        }

        request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        request.setWardenRemarks(actionRequest.getWardenRemarks().trim());

        request = leaveRequestRepository.save(request);
        return mapToDTO(request);
    }

    public LeaveRequestDTO mapToDTO(LeaveRequest req) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(req.getId());
        dto.setStudentId(req.getStudent().getId());
        dto.setStudentName(req.getStudent().getFullName());
        dto.setRoom(req.getStudent().getAssignedRoom() != null ? req.getStudent().getAssignedRoom().getId() : "Unassigned");
        dto.setLeaveType(req.getLeaveType());
        dto.setStartDate(req.getStartDate());
        dto.setEndDate(req.getEndDate());
        dto.setParentContact(req.getParentContact());
        dto.setReason(req.getReason());
        
        // Convert status enum to title case for frontend UI (Pending, Approved, Rejected)
        String statusStr = req.getStatus().name();
        String formattedStatus = statusStr.substring(0, 1).toUpperCase() + statusStr.substring(1).toLowerCase();
        dto.setStatus(formattedStatus);

        dto.setAppliedDate(req.getCreatedAt() != null ? req.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now().toString());
        dto.setWardenNotes(req.getWardenRemarks() != null ? req.getWardenRemarks() : "");
        return dto;
    }
}
