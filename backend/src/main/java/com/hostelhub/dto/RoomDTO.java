package com.hostelhub.dto;

import java.util.ArrayList;
import java.util.List;

public class RoomDTO {

    private String id;
    private String block;
    private String number;
    private Integer floor;
    private String type;
    private Integer capacity;
    private Integer currentOccupancy;
    private String status; // "booked" or "remaining"
    private List<String> occupants = new ArrayList<>();

    public RoomDTO() {}

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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getOccupants() {
        return occupants;
    }

    public void setOccupants(List<String> occupants) {
        this.occupants = occupants;
    }
}
