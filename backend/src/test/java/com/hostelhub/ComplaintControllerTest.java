package com.hostelhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.ComplaintActionRequest;
import com.hostelhub.dto.ComplaintDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.hostelhub.service.AuthService authService;

    @Autowired
    private com.hostelhub.service.ComplaintService complaintService;

    @Autowired
    private com.hostelhub.repository.StudentRepository studentRepository;

    private String testStudentId;
    private String testComplaintId;

    @org.junit.jupiter.api.BeforeEach
    public void setupFixture() {
        if (!studentRepository.existsByEmail("test.student@student.edu")) {
            com.hostelhub.dto.StudentRegisterRequest req = new com.hostelhub.dto.StudentRegisterRequest();
            req.setFullName("Test Student");
            req.setEmail("test.student@student.edu");
            req.setPassword("student123");
            req.setPhone("1234567890");
            req.setDepartment("Computer Science");
            req.setYear("1st Year");
            req.setGender("Male");
            req.setEmergencyContact("0987654321");
            req.setRoomNeeds("Single AC");
            com.hostelhub.dto.StudentDTO s = authService.registerStudent(req);
            testStudentId = s.getId();
        } else {
            testStudentId = studentRepository.findByEmail("test.student@student.edu").get().getId();
        }

        ComplaintDTO dto = new ComplaintDTO();
        dto.setStudentId(testStudentId);
        dto.setCategory("Electrical");
        dto.setPriority("Medium");
        dto.setDescription("Light bulb flickering in reading area.");
        ComplaintDTO created = complaintService.submitComplaint(dto);
        testComplaintId = created.getId();
    }

    @Test
    @WithMockUser(username = "test.student@student.edu", roles = {"STUDENT"})
    @DisplayName("Test Raise Complaint - Student")
    public void testSubmitComplaintStudent() throws Exception {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setStudentId(testStudentId);
        dto.setCategory("Electrical");
        dto.setPriority("High");
        dto.setDescription("Light switch spark when turned on.");

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("Pending"));
    }

    @Test
    @WithMockUser(username = "admin@hms.edu", roles = {"ADMIN"})
    @DisplayName("Test Update Complaint Status - Admin/Warden")
    public void testUpdateComplaintWarden() throws Exception {
        ComplaintActionRequest action = new ComplaintActionRequest("Resolved", "Fixed switch box.");

        mockMvc.perform(put("/api/complaints/" + testComplaintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Resolved"));
    }
}
