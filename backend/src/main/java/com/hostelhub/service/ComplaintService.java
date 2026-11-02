package com.hostelhub.service;

import com.hostelhub.dto.ComplaintActionRequest;
import com.hostelhub.dto.ComplaintDTO;
import com.hostelhub.entity.Complaint;
import com.hostelhub.entity.Student;
import com.hostelhub.exception.BadRequestException;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.hostelhub.repository.UserRepository userRepository;

    @Autowired
    private com.hostelhub.repository.WardenRepository wardenRepository;

    public List<ComplaintDTO> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ComplaintDTO> getComplaintsForUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) return getAllComplaints();

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
                return getComplaintsByStudent(student.getId());
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
                return complaintRepository.findAll().stream()
                        .filter(c -> c.getStudent() == null || c.getStudent().getAssignedRoom() == null
                                || block.equalsIgnoreCase(c.getStudent().getAssignedRoom().getBlock()))
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
            }
        }

        return getAllComplaints();
    }

    public List<ComplaintDTO> getComplaintsByStudent(String studentId) {
        return complaintRepository.findByStudentId(studentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintDTO submitComplaint(ComplaintDTO dto, String authenticatedUsername) {
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
            throw new BadRequestException("Valid student ID is required for complaint submission");
        }

        long maxId = 500;
        for (Complaint c : complaintRepository.findAll()) {
            if (c.getId() != null && c.getId().toUpperCase().startsWith("CMP-")) {
                try {
                    long num = Long.parseLong(c.getId().substring(4));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        String cmpId = String.format("CMP-%d", maxId + 1);

        Complaint.Priority priority = Complaint.Priority.MEDIUM;
        if (dto.getPriority() != null) {
            try {
                priority = Complaint.Priority.valueOf(dto.getPriority().toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {}
        }

        Complaint complaint = new Complaint(
                cmpId,
                student,
                dto.getCategory().trim(),
                priority,
                dto.getDescription().trim()
        );

        complaint = complaintRepository.save(complaint);
        return mapToDTO(complaint);
    }

    @Transactional
    public ComplaintDTO submitComplaint(ComplaintDTO dto) {
        return submitComplaint(dto, null);
    }

    @Transactional
    public ComplaintDTO updateComplaint(String id, ComplaintActionRequest actionRequest) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint ticket not found with ID: " + id));

        if (actionRequest.getStatus() != null) {
            String statusUpper = actionRequest.getStatus().toUpperCase().replace(" ", "_");
            try {
                complaint.setStatus(Complaint.ComplaintStatus.valueOf(statusUpper));
            } catch (IllegalArgumentException ex) {
                if ("IN_PROGRESS".equalsIgnoreCase(statusUpper) || "IN PROGRESS".equalsIgnoreCase(statusUpper)) {
                    complaint.setStatus(Complaint.ComplaintStatus.IN_PROGRESS);
                } else if ("RESOLVED".equalsIgnoreCase(statusUpper)) {
                    complaint.setStatus(Complaint.ComplaintStatus.RESOLVED);
                } else {
                    complaint.setStatus(Complaint.ComplaintStatus.PENDING);
                }
            }
        }

        if (actionRequest.getRemarks() != null) {
            complaint.setRemarks(actionRequest.getRemarks());
        }

        complaint = complaintRepository.save(complaint);
        return mapToDTO(complaint);
    }

    public ComplaintDTO mapToDTO(Complaint c) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(c.getId());
        dto.setStudentId(c.getStudent().getId());
        dto.setStudentName(c.getStudent().getFullName());
        dto.setRoom(c.getStudent().getAssignedRoom() != null ? c.getStudent().getAssignedRoom().getId() : "Unassigned");
        dto.setCategory(c.getCategory());
        dto.setDescription(c.getDescription());

        // Map priority string: "Low", "Medium", "High"
        String prio = c.getPriority().name();
        dto.setPriority(prio.substring(0, 1).toUpperCase() + prio.substring(1).toLowerCase());

        // Map status for frontend: "Pending", "In Progress", "Resolved"
        String statusStr = c.getStatus() == Complaint.ComplaintStatus.IN_PROGRESS ? "In Progress" :
                c.getStatus().name().substring(0, 1).toUpperCase() + c.getStatus().name().substring(1).toLowerCase();
        dto.setStatus(statusStr);

        dto.setDate(c.getCreatedAt() != null ? c.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now().toString());
        dto.setRemarks(c.getRemarks() != null ? c.getRemarks() : "");
        return dto;
    }
}
