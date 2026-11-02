package com.hostelhub.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_logs")
public class GateLog {

    public enum LogType {
        IN, OUT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "scan_time", nullable = false)
    private LocalDateTime scanTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LogType type;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "warden_id")
    private String wardenId;

    @Column(name = "parent_contact")
    private String parentContact;

    public GateLog() {}

    public GateLog(String studentId, String studentName, LocalDate logDate, LocalDateTime scanTime, LogType type, String roomNumber, String wardenId, String parentContact) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.logDate = logDate;
        this.scanTime = scanTime;
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

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
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
