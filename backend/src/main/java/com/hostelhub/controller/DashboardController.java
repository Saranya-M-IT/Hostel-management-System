package com.hostelhub.controller;

import com.hostelhub.dto.AdminDashboardDTO;
import com.hostelhub.dto.StudentDashboardDTO;
import com.hostelhub.dto.WardenDashboardDTO;
import com.hostelhub.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboards", description = "Role-specific Dashboard Analytics APIs")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin")
    @Operation(summary = "Get admin overall analytics metrics")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping({"/warden", "/warden/{wardenId}"})
    @Operation(summary = "Get warden dashboard operational counts")
    public ResponseEntity<WardenDashboardDTO> getWardenDashboard(
            @PathVariable(required = false) String wardenId,
            org.springframework.security.core.Authentication authentication) {
        String target = wardenId;
        if (target == null && authentication != null) {
            target = authentication.getName();
        }
        return ResponseEntity.ok(dashboardService.getWardenDashboard(target));
    }

    @GetMapping({"/student/me", "/student"})
    @Operation(summary = "Get currently authenticated student dashboard metrics")
    public ResponseEntity<StudentDashboardDTO> getCurrentStudentDashboard(org.springframework.security.core.Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        if (username == null) {
            throw new com.hostelhub.exception.BadRequestException("Authentication required");
        }
        return ResponseEntity.ok(dashboardService.getStudentDashboardForUsername(username));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student specific dashboard metrics")
    public ResponseEntity<StudentDashboardDTO> getStudentDashboard(
            @PathVariable String studentId,
            org.springframework.security.core.Authentication authentication) {
        if ("me".equalsIgnoreCase(studentId) && authentication != null) {
            return ResponseEntity.ok(dashboardService.getStudentDashboardForUsername(authentication.getName()));
        }
        return ResponseEntity.ok(dashboardService.getStudentDashboard(studentId));
    }
}
