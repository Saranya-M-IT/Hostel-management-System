package com.hostelhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.LoginRequest;
import com.hostelhub.dto.StudentRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.hostelhub.service.WardenService wardenService;

    @Autowired
    private com.hostelhub.repository.WardenRepository wardenRepository;

    @Test
    @DisplayName("Test Admin Login - Success")
    public void testAdminLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@hms.edu", "admin123", "ADMIN");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Test Warden Login - Success")
    public void testWardenLoginSuccess() throws Exception {
        if (!wardenRepository.existsByEmail("test.warden@hms.edu")) {
            com.hostelhub.dto.WardenDTO w = new com.hostelhub.dto.WardenDTO();
            w.setName("Test Warden");
            w.setEmail("test.warden@hms.edu");
            w.setPhone("1234567890");
            w.setBlock("Block A");
            w.setPassword("warden123");
            wardenService.addWarden(w);
        }

        LoginRequest loginRequest = new LoginRequest("test.warden@hms.edu", "warden123", "WARDEN");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("WARDEN"));
    }

    @Test
    @DisplayName("Test Login - Invalid Password")
    public void testLoginInvalidPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@hms.edu", "wrongpassword", "ADMIN");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test Student Registration - Success")
    public void testStudentRegisterSuccess() throws Exception {
        StudentRegisterRequest reg = new StudentRegisterRequest();
        reg.setFullName("John Test");
        reg.setEmail("john.test@student.edu");
        reg.setPassword("student123");
        reg.setPhone("5558889999");
        reg.setDepartment("Computer Science");
        reg.setYear("1st Year");
        reg.setGender("Male");
        reg.setEmergencyContact("5550001111");
        reg.setRoomNeeds("Single AC Room");

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("john.test@student.edu"));
    }
}
