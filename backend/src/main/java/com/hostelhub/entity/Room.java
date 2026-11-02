package com.hostelhub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    public enum RoomStatus {
        VACANT, PARTIALLY_OCCUPIED, FULL, AVAILABLE, REMAINING, BOOKED
    }

    @Id
    @Column(nullable = false)
    private String id; // e.g. "A-101"

    @Column(nullable = false)
    private String block; // e.g. "Block A"

    @Column(name = "room_number", nullable = false)
    private String roomNumber; // e.g. "101"

    @Column(nullable = false)
    private Integer floor; // e.g. 1

    @Column(name = "room_type", nullable = false)
    private String roomType; // e.g. "Double AC"

    @Column(nullable = false)
    private Integer capacity; // e.g. 2

    @Column(name = "current_occupancy", nullable = false)
    private Integer currentOccupancy = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.VACANT;

    public Room() {}

    public Room(String id, String block, String roomNumber, Integer floor, String roomType, Integer capacity) {
        this.id = id;
        this.block = block;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.roomType = roomType;
        this.capacity = capacity;
        this.currentOccupancy = 0;
        this.status = RoomStatus.VACANT;
    }

    public void updateStatusBasedOnOccupancy() {
        if (currentOccupancy == 0) {
            this.status = RoomStatus.VACANT;
        } else if (currentOccupancy >= capacity) {
            this.status = RoomStatus.FULL;
        } else {
            this.status = RoomStatus.PARTIALLY_OCCUPIED;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
        updateStatusBasedOnOccupancy();
    }

    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
        updateStatusBasedOnOccupancy();
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}
