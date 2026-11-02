package com.hostelhub.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GateLogDTO {

    private Long id;
    private String studentId;
    private String studentName;
    private LocalDate logDate;
    private LocalDateTime scanTime;
    private String scanTimeFormatted;
    private String logDateFormatted;
    private String type; // "IN" or "OUT"
    private String roomNumber;
    private String wardenId;
    private String parentContact;

    public GateLogDTO() {}

    public GateLogDTO(Long id, String studentId, String studentName, LocalDate logDate, LocalDateTime scanTime, String scanTimeFormatted, String logDateFormatted, String type, String roomNumber, String wardenId, String parentContact) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.logDate = logDate;
        this.scanTime = scanTime;
        this.scanTimeFormatted = scanTimeFormatted;
        this.logDateFormatted = logDateFormatted;
        this.type = type;
        this.roomNumber = roomNumber;
        this.wardenId = wardenId;
        this.parentContact = parentContact;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public LocalDateTime getScanTime() {
        return scanTime;
    }

    public void setScanTime(LocalDateTime scanTime) {
        this.scanTime = scanTime;
    }

    public String getScanTimeFormatted() {
        return scanTimeFormatted;
    }

    public void setScanTimeFormatted(String scanTimeFormatted) {
        this.scanTimeFormatted = scanTimeFormatted;
    }

    public String getLogDateFormatted() {
        return logDateFormatted;
    }

    public void setLogDateFormatted(String logDateFormatted) {
        this.logDateFormatted = logDateFormatted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getWardenId() {
        return wardenId;
    }

    public void setWardenId(String wardenId) {
        this.wardenId = wardenId;
    }

    public String getParentContact() {
        return parentContact;
    }

    public void setParentContact(String parentContact) {
        this.parentContact = parentContact;
    }
}
