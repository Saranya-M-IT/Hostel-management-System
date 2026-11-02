package com.hostelhub.dto;

import jakarta.validation.constraints.NotBlank;

public class AllocationRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Room ID is required")
    private String roomId;

    public AllocationRequest() {}

    public AllocationRequest(String studentId, String roomId) {
        this.studentId = studentId;
        this.roomId = roomId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
