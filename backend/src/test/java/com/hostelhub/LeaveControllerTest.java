package com.hostelhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.LeaveActionRequest;
import com.hostelhub.dto.LeaveRequestDTO;
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
public class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.hostelhub.service.AuthService authService;

    @Autowired
    private com.hostelhub.service.LeaveService leaveService;

    @Autowired
    private com.hostelhub.repository.StudentRepository studentRepository;

    private String testStudentId;
    private String testLeaveId;

    @org.junit.jupiter.api.BeforeEach
    public void setupFixture() {
        if (!studentRepository.existsByEmail("leave.student@student.edu")) {
            com.hostelhub.dto.StudentRegisterRequest req = new com.hostelhub.dto.StudentRegisterRequest();
            req.setFullName("Leave Student");
            req.setEmail("leave.student@student.edu");
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
            testStudentId = studentRepository.findByEmail("leave.student@student.edu").get().getId();
        }

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setStudentId(testStudentId);
        dto.setLeaveType("Home Leave");
        dto.setStartDate(java.time.LocalDate.now().toString());
        dto.setEndDate(java.time.LocalDate.now().plusDays(3).toString());
        dto.setParentContact("0987654321");
        dto.setReason("Weekend visit home.");
        LeaveRequestDTO created = leaveService.submitLeaveRequest(dto);
        testLeaveId = created.getId();
    }

    @Test
    @WithMockUser(username = "leave.student@student.edu", roles = {"STUDENT"})
    @DisplayName("Test Submit Leave Request - Student")
    public void testSubmitLeaveRequest() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setStudentId(testStudentId);
        dto.setLeaveType("Home Leave");
        dto.setStartDate(java.time.LocalDate.now().toString());
        dto.setEndDate(java.time.LocalDate.now().plusDays(5).toString());
        dto.setParentContact("5559990011");
        dto.setReason("Visiting home for festival.");

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("Pending"));
    }

    @Test
    @WithMockUser(username = "admin@hms.edu", roles = {"ADMIN"})
    @DisplayName("Test Approve Leave Request - Admin/Warden")
    public void testApproveLeaveRequestWarden() throws Exception {
        LeaveActionRequest action = new LeaveActionRequest("APPROVED", "Permission granted.");

        mockMvc.perform(put("/api/leaves/" + testLeaveId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Approved"));
    }
}
