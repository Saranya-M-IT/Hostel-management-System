package com.hostelhub.controller;

import com.hostelhub.dto.LeaveActionRequest;
import com.hostelhub.dto.LeaveRequestDTO;
import com.hostelhub.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Requests", description = "Student Leave Request & Warden Approval APIs")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping
    @Operation(summary = "Get leave requests (filtered according to role)")
    public ResponseEntity<List<LeaveRequestDTO>> getAllLeaveRequests(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(leaveService.getLeaveRequestsForUser(authentication));
    }

    @GetMapping("/my")
    @Operation(summary = "Get leave requests for currently authenticated student")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaveRequests(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(leaveService.getLeaveRequestsForUser(authentication));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get leave requests for specific student")
    public ResponseEntity<List<LeaveRequestDTO>> getLeaveRequestsByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(leaveService.getLeaveRequestsByStudent(studentId));
    }

    @PostMapping
    @Operation(summary = "Submit new leave request letter")
    public ResponseEntity<LeaveRequestDTO> submitLeaveRequest(
            @Valid @RequestBody LeaveRequestDTO dto,
            org.springframework.security.core.Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        LeaveRequestDTO created = leaveService.submitLeaveRequest(dto, username);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping({"/{id}/action", "/{id}/approve"})
    @Operation(summary = "Approve or process leave request (Warden)")
    public ResponseEntity<LeaveRequestDTO> approveLeaveRequest(@PathVariable String id, @Valid @RequestBody(required = false) LeaveActionRequest actionRequest) {
        if (actionRequest != null && "Rejected".equalsIgnoreCase(actionRequest.getAction())) {
            return ResponseEntity.ok(leaveService.rejectLeaveRequest(id, actionRequest));
        }
        return ResponseEntity.ok(leaveService.approveLeaveRequest(id, actionRequest));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject leave request (Warden)")
    public ResponseEntity<LeaveRequestDTO> rejectLeaveRequest(@PathVariable String id, @Valid @RequestBody(required = false) LeaveActionRequest actionRequest) {
        return ResponseEntity.ok(leaveService.rejectLeaveRequest(id, actionRequest));
    }
}
