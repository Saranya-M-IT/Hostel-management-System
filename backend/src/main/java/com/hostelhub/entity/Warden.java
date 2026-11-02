package com.hostelhub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "wardens")
public class Warden {

    @Id
    private String id; // e.g. "WRD-201"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "assigned_block", nullable = false)
    private String assignedBlock; // e.g. "Block A"

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Warden() {}

    public Warden(String id, String name, String email, String phone, String assignedBlock) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.assignedBlock = assignedBlock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAssignedBlock() {
        return assignedBlock;
    }

    public void setAssignedBlock(String assignedBlock) {
        this.assignedBlock = assignedBlock;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
