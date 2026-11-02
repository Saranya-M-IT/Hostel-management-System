package com.hostelhub.controller;

import com.hostelhub.dto.GateLogDTO;
import com.hostelhub.dto.GateScanRequest;
import com.hostelhub.dto.GateScanResponse;
import com.hostelhub.entity.GateLog;
import com.hostelhub.service.GateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Gate Management", description = "Common Hostel QR Code Scanning, IN/OUT Gate Logs and Notifications")
public class GateController {

    @Autowired
    private GateService gateService;

    @GetMapping("/hostel/qr")
    @Operation(summary = "Get single common hostel QR code token")
    public ResponseEntity<Map<String, String>> getCommonQRCode() {
        String token = gateService.getActiveCommonQRCodeToken();
        return ResponseEntity.ok(Collections.singletonMap("qrToken", token));
    }

    @PostMapping("/gate/scan")
    @Operation(summary = "Scan common hostel QR code and record IN/OUT log for authenticated student")
    public ResponseEntity<GateScanResponse> scanQR(@Valid @RequestBody GateScanRequest request, Authentication authentication) {
        String username = authentication.getName();
        GateScanResponse response = gateService.processScan(request.getQrToken(), username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/gate/my-logs")
    @Operation(summary = "Get gate logs for authenticated student")
    public ResponseEntity<List<GateLogDTO>> getMyLogs(Authentication authentication) {
        String username = authentication.getName();
        List<GateLogDTO> logs = gateService.getStudentLogs(username);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/gate/warden/logs")
    @Operation(summary = "Get student gate logs for warden dashboard with optional filters")
    public ResponseEntity<List<GateLogDTO>> getWardenLogs(
            Authentication authentication,
            @RequestParam(required = false) String student,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) GateLog.LogType type
    ) {
        String username = authentication.getName();
        List<GateLogDTO> logs = gateService.getWardenLogs(username, student, room, date, type);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/gate/admin/logs")
    @Operation(summary = "Get all hostel gate logs for admin monitoring with optional filters")
    public ResponseEntity<List<GateLogDTO>> getAdminLogs(
            @RequestParam(required = false) String student,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) GateLog.LogType type
    ) {
        List<GateLogDTO> logs = gateService.getAdminLogs(student, room, date, type);
        return ResponseEntity.ok(logs);
    }
}
