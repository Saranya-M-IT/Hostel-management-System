package com.hostelhub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RoomCreateRequest {

    @NotBlank(message = "Block name is required")
    private String block;

    @NotBlank(message = "Room number is required")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Room number must contain letters, numbers, or hyphens")
    private String number;

    @NotNull(message = "Floor is required")
    @Min(value = 0, message = "Floor must be 0 or higher")
    private Integer floor;

    @NotBlank(message = "Room type is required")
    private String type;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    public RoomCreateRequest() {}

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
}
