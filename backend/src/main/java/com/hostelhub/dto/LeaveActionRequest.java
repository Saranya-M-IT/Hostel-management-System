package com.hostelhub.dto;

import jakarta.validation.constraints.NotBlank;

public class LeaveActionRequest {

    @NotBlank(message = "Status is required")
    private String status; // "APPROVED" or "REJECTED"

    private String wardenRemarks;

    public LeaveActionRequest() {}

    public LeaveActionRequest(String status, String wardenRemarks) {
        this.status = status;
        this.wardenRemarks = wardenRemarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAction() {
        return status;
    }

    public void setAction(String action) {
        this.status = action;
    }

    public String getWardenRemarks() {
        return wardenRemarks;
    }

    public void setWardenRemarks(String wardenRemarks) {
        this.wardenRemarks = wardenRemarks;
    }
}
