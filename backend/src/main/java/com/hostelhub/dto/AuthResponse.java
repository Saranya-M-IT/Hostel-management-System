package com.hostelhub.dto;

public class AuthResponse {

    private String token;
    private String role;
    private Long userId;
    private String name;
    private String studentId;
    private String wardenId;

    public AuthResponse() {}

    public AuthResponse(String token, String role, Long userId, String name, String studentId, String wardenId) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.studentId = studentId;
        this.wardenId = wardenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getWardenId() {
        return wardenId;
    }

    public void setWardenId(String wardenId) {
        this.wardenId = wardenId;
    }
}
