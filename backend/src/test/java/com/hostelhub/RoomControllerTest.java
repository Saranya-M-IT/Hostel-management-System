package com.hostelhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelhub.dto.AllocationRequest;
import com.hostelhub.dto.RoomCreateRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test Get All Rooms - Public Access")
    public void testGetAllRooms() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@hms.edu", roles = {"ADMIN"})
    @DisplayName("Test Create Room - Admin Authorized")
    public void testCreateRoomAdmin() throws Exception {
        RoomCreateRequest request = new RoomCreateRequest();
        request.setBlock("Block C");
        request.setNumber("305");
        request.setFloor(3);
        request.setType("Double AC");
        request.setCapacity(2);

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("C-305"));
    }

    @Autowired
    private com.hostelhub.service.AuthService authService;

    @Autowired
    private com.hostelhub.repository.StudentRepository studentRepository;

    private String testStudentId;

    @org.junit.jupiter.api.BeforeEach
    public void setupFixture() {
        if (!studentRepository.existsByEmail("room.student@student.edu")) {
            com.hostelhub.dto.StudentRegisterRequest req = new com.hostelhub.dto.StudentRegisterRequest();
            req.setFullName("Room Student");
            req.setEmail("room.student@student.edu");
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
            testStudentId = studentRepository.findByEmail("room.student@student.edu").get().getId();
        }
    }

    @Test
    @WithMockUser(username = "admin@hms.edu", roles = {"ADMIN"})
    @DisplayName("Test Allocate Room - Admin/Warden Authorized")
    public void testAllocateRoomWarden() throws Exception {
        AllocationRequest request = new AllocationRequest(testStudentId, "C-101");

        mockMvc.perform(post("/api/rooms/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("C-101"));
    }

    @Test
    @WithMockUser(username = "alex.m@student.edu", roles = {"STUDENT"})
    @DisplayName("Test Create Room - Student Access Denied (403)")
    public void testCreateRoomStudentForbidden() throws Exception {
        RoomCreateRequest request = new RoomCreateRequest();
        request.setBlock("Block A");
        request.setNumber("999");
        request.setFloor(9);
        request.setType("Single");
        request.setCapacity(1);

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
