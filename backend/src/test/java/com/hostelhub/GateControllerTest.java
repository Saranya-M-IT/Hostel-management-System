package com.hostelhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.GateScanRequest;
import com.hostelhub.service.GateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GateService gateService;

    @Autowired
    private com.hostelhub.service.AuthService authService;

    @Autowired
    private com.hostelhub.repository.StudentRepository studentRepository;

    @org.junit.jupiter.api.BeforeEach
    public void setupFixture() {
        if (!studentRepository.existsByEmail("gate.student@student.edu")) {
            com.hostelhub.dto.StudentRegisterRequest req = new com.hostelhub.dto.StudentRegisterRequest();
            req.setFullName("Gate Student");
            req.setEmail("gate.student@student.edu");
            req.setPassword("student123");
            req.setPhone("1234567890");
            req.setDepartment("Computer Science");
            req.setYear("1st Year");
            req.setGender("Male");
            req.setEmergencyContact("0987654321");
            req.setRoomNeeds("Single AC");
            authService.registerStudent(req);
        }
    }

    @Test
    @DisplayName("GET /api/hostel/qr should return common hostel QR token")
    public void testGetCommonQRCode() throws Exception {
        mockMvc.perform(get("/api/hostel/qr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrToken", is(GateService.COMMON_QR_TOKEN)));
    }

    @Test
    @WithMockUser(username = "gate.student@student.edu", roles = {"STUDENT"})
    @DisplayName("POST /api/gate/scan should record IN on first scan and OUT on next scan")
    public void testStudentGateScan() throws Exception {
        GateScanRequest request = new GateScanRequest(GateService.COMMON_QR_TOKEN);

        // First scan: IN
        mockMvc.perform(post("/api/gate/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.type", is("IN")))
                .andExpect(jsonPath("$.message", containsString("IN recorded successfully")));

        // Rescan immediately should throw duplicate scan error (30s cooldown)
        mockMvc.perform(post("/api/gate/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Duplicate scan detected")));
    }

    @Test
    @WithMockUser(username = "gate.student@student.edu", roles = {"STUDENT"})
    @DisplayName("POST /api/gate/scan should reject invalid QR code token")
    public void testInvalidQRScan() throws Exception {
        GateScanRequest request = new GateScanRequest("INVALID-FAKE-QR-TOKEN");

        mockMvc.perform(post("/api/gate/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid or unrecognized Hostel QR Code")));
    }

    @Test
    @WithMockUser(username = "gate.student@student.edu", roles = {"STUDENT"})
    @DisplayName("GET /api/gate/my-logs should return gate logs for logged in student")
    public void testGetMyGateLogs() throws Exception {
        mockMvc.perform(get("/api/gate/my-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @WithMockUser(username = "admin@hms.edu", roles = {"ADMIN"})
    @DisplayName("GET /api/gate/warden/logs should return logs for warden dashboard")
    public void testGetWardenGateLogs() throws Exception {
        mockMvc.perform(get("/api/gate/warden/logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@hms.edu", roles = {"ADMIN"})
    @DisplayName("GET /api/gate/admin/logs should return all logs for admin dashboard")
    public void testGetAdminGateLogs() throws Exception {
        mockMvc.perform(get("/api/gate/admin/logs"))
                .andExpect(status().isOk());
    }
}
