package com.hostelhub.controller;

import com.hostelhub.dto.ComplaintActionRequest;
import com.hostelhub.dto.ComplaintDTO;
import com.hostelhub.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@Tag(name = "Complaints", description = "Student Maintenance Complaint Ticket APIs")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping
    @Operation(summary = "Get complaints (filtered according to role)")
    public ResponseEntity<List<ComplaintDTO>> getAllComplaints(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(complaintService.getComplaintsForUser(authentication));
    }

    @GetMapping("/my")
    @Operation(summary = "Get complaints for currently authenticated student")
    public ResponseEntity<List<ComplaintDTO>> getMyComplaints(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(complaintService.getComplaintsForUser(authentication));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get complaints for specific student")
    public ResponseEntity<List<ComplaintDTO>> getComplaintsByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(complaintService.getComplaintsByStudent(studentId));
    }

    @PostMapping
    @Operation(summary = "Raise a new complaint ticket")
    public ResponseEntity<ComplaintDTO> submitComplaint(
            @Valid @RequestBody ComplaintDTO dto,
            org.springframework.security.core.Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        ComplaintDTO created = complaintService.submitComplaint(dto, username);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping({"/{id}", "/{id}/action"})
    @Operation(summary = "Update complaint status & warden/technician remarks")
    public ResponseEntity<ComplaintDTO> updateComplaint(@PathVariable String id, @Valid @RequestBody ComplaintActionRequest actionRequest) {
        return ResponseEntity.ok(complaintService.updateComplaint(id, actionRequest));
    }
}
