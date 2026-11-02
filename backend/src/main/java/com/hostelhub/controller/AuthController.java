package com.hostelhub.controller;

import com.hostelhub.dto.AuthResponse;
import com.hostelhub.dto.LoginRequest;
import com.hostelhub.dto.StudentDTO;
import com.hostelhub.dto.StudentRegisterRequest;
import com.hostelhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and Registration APIs for Admin, Warden, and Student")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/register/student", "/register-student"})
    @Operation(summary = "Register new student account")
    public ResponseEntity<StudentDTO> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        StudentDTO student = authService.registerStudent(request);
        return new ResponseEntity<>(student, HttpStatus.CREATED);
    }
}
