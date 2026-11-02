package com.hostelhub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    private String id; // e.g. "STU-1001"

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String department;

    @Column(name = "student_year", nullable = false)
    private String year; // e.g. "1st Year", "3rd Year"

    @Column(nullable = false)
    private String gender;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "room_needs", length = 1000)
    private String roomNeeds;

    @Column(name = "allocation_status", nullable = false)
    private String allocationStatus = "Pending Allocation";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_room_id")
    private Room assignedRoom;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Student() {}

    public Student(String id, String fullName, String email, String phone, String department, String year, String gender, String emergencyContact, String roomNeeds) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.year = year;
        this.gender = gender;
        this.emergencyContact = emergencyContact;
        this.roomNeeds = roomNeeds;
        this.allocationStatus = "Pending Allocation";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getRoomNeeds() {
        return roomNeeds;
    }

    public void setRoomNeeds(String roomNeeds) {
        this.roomNeeds = roomNeeds;
    }

    public String getAllocationStatus() {
        return allocationStatus;
    }

    public void setAllocationStatus(String allocationStatus) {
        this.allocationStatus = allocationStatus;
    }

    public Room getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(Room assignedRoom) {
        this.assignedRoom = assignedRoom;
        if (assignedRoom != null) {
            this.allocationStatus = "Allocated";
        } else {
            this.allocationStatus = "Pending Allocation";
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
