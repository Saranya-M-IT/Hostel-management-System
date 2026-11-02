package com.hostelhub.controller;

import com.hostelhub.dto.HostelRuleDTO;
import com.hostelhub.service.HostelRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@Tag(name = "Hostel Rules", description = "Hostel Rules & Policy Management APIs")
public class HostelRuleController {

    @Autowired
    private HostelRuleService hostelRuleService;

    @GetMapping
    @Operation(summary = "Get all hostel rules")
    public ResponseEntity<List<HostelRuleDTO>> getAllRules() {
        return ResponseEntity.ok(hostelRuleService.getAllRules());
    }

    @PostMapping
    @Operation(summary = "Create new hostel rule (Admin)")
    public ResponseEntity<HostelRuleDTO> addRule(@Valid @RequestBody HostelRuleDTO dto) {
        HostelRuleDTO created = hostelRuleService.addRule(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update hostel rule (Admin)")
    public ResponseEntity<HostelRuleDTO> updateRule(@PathVariable String id, @Valid @RequestBody HostelRuleDTO dto) {
        return ResponseEntity.ok(hostelRuleService.updateRule(id, dto));
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "Toggle active/inactive status of hostel rule")
    public ResponseEntity<HostelRuleDTO> toggleRule(@PathVariable String id) {
        return ResponseEntity.ok(hostelRuleService.toggleRule(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete hostel rule (Admin)")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        hostelRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
