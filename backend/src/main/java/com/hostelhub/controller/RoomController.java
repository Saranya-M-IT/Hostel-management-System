package com.hostelhub.controller;

import com.hostelhub.dto.AllocationRequest;
import com.hostelhub.dto.RoomCreateRequest;
import com.hostelhub.dto.RoomDTO;
import com.hostelhub.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "Room Management & Allocation APIs")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    @Operation(summary = "Get all hostel rooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID (e.g., A-101)")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable String id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping
    @Operation(summary = "Create new hostel room")
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        RoomDTO room = roomService.createRoom(request);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update room details")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable String id, @Valid @RequestBody RoomCreateRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete hostel room")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vacant")
    @Operation(summary = "Get all vacant/remaining rooms")
    public ResponseEntity<List<RoomDTO>> getVacantRooms() {
        return ResponseEntity.ok(roomService.getVacantRooms());
    }

    @PostMapping("/allocate")
    @Operation(summary = "Allocate vacant room to a student")
    public ResponseEntity<RoomDTO> allocateRoom(@Valid @RequestBody AllocationRequest request) {
        RoomDTO room = roomService.allocateRoom(request);
        return ResponseEntity.ok(room);
    }
}
