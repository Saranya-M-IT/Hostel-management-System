package com.hostelhub.controller;

import com.hostelhub.dto.RoomDTO;
import com.hostelhub.dto.StudentDTO;
import com.hostelhub.dto.StudentRegisterRequest;
import com.hostelhub.service.AuthService;
import com.hostelhub.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student Directory and Account Management APIs")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated student profile")
    public ResponseEntity<StudentDTO> getCurrentStudent(org.springframework.security.core.Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(studentService.getStudentByUsername(username));
    }

    @PutMapping("/me")
    @Operation(summary = "Update currently authenticated student profile")
    public ResponseEntity<StudentDTO> updateCurrentStudent(
            org.springframework.security.core.Authentication authentication,
            @Valid @RequestBody StudentDTO dto) {
        String username = authentication.getName();
        return ResponseEntity.ok(studentService.updateStudentByUsername(username, dto));
    }

    @GetMapping("/me/room")
    @Operation(summary = "Get assigned room details for currently authenticated student")
    public ResponseEntity<RoomDTO> getCurrentStudentRoom(org.springframework.security.core.Authentication authentication) {
        String username = authentication.getName();
        StudentDTO me = studentService.getStudentByUsername(username);
        return ResponseEntity.ok(studentService.getStudentRoom(me.getId()));
    }

    @GetMapping
    @Operation(summary = "Get registered students (filtered according to role)")
    public ResponseEntity<List<StudentDTO>> getAllStudents(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(studentService.getStudentsForUser(authentication));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student profile by ID")
    public ResponseEntity<StudentDTO> getStudentById(
            @PathVariable String id,
            org.springframework.security.core.Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            StudentDTO me = studentService.getStudentByUsername(authentication.getName());
            if (!me.getId().equalsIgnoreCase(id)) {
                throw new com.hostelhub.exception.BadRequestException("Access Denied: You cannot view other students' private records.");
            }
        }
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    @Operation(summary = "Create new student record")
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentRegisterRequest request) {
        StudentDTO student = authService.registerStudent(request);
        return new ResponseEntity<>(student, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student profile & needs")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable String id, @Valid @RequestBody StudentDTO dto) {
        return ResponseEntity.ok(studentService.updateStudent(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student record")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/room")
    @Operation(summary = "Get assigned room details for a student")
    public ResponseEntity<RoomDTO> getStudentRoom(@PathVariable String id) {
        return ResponseEntity.ok(studentService.getStudentRoom(id));
    }

    @PostMapping("/{id}/vacate")
    @Operation(summary = "Vacate student from assigned room")
    public ResponseEntity<StudentDTO> vacateStudent(@PathVariable String id) {
        return ResponseEntity.ok(studentService.vacateStudent(id));
    }
}
