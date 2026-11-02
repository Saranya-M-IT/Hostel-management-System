package com.hostelhub.controller;

import com.hostelhub.dto.WardenDTO;
import com.hostelhub.service.WardenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wardens")
@Tag(name = "Wardens", description = "Warden Officer Management APIs (Admin Only)")
public class WardenController {

    @Autowired
    private WardenService wardenService;

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated warden officer profile")
    public ResponseEntity<WardenDTO> getCurrentWarden(org.springframework.security.core.Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(wardenService.getWardenByUsername(username));
    }

    @GetMapping
    @Operation(summary = "Get all hostel wardens")
    public ResponseEntity<List<WardenDTO>> getAllWardens() {
        return ResponseEntity.ok(wardenService.getAllWardens());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warden details by ID")
    public ResponseEntity<WardenDTO> getWardenById(@PathVariable String id) {
        return ResponseEntity.ok(wardenService.getWardenById(id));
    }

    @PostMapping
    @Operation(summary = "Add new warden officer")
    public ResponseEntity<WardenDTO> addWarden(@Valid @RequestBody WardenDTO dto) {
        WardenDTO warden = wardenService.addWarden(dto);
        return new ResponseEntity<>(warden, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update warden details")
    public ResponseEntity<WardenDTO> updateWarden(@PathVariable String id, @Valid @RequestBody WardenDTO dto) {
        return ResponseEntity.ok(wardenService.updateWarden(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warden officer")
    public ResponseEntity<Void> deleteWarden(@PathVariable String id) {
        wardenService.deleteWarden(id);
        return ResponseEntity.noContent().build();
    }
}
