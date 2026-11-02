package com.hostelhub.dto;

import jakarta.validation.constraints.NotBlank;

public class ComplaintActionRequest {

    @NotBlank(message = "Status is required")
    private String status; // "Pending", "In Progress", "Resolved"

    private String remarks;

    public ComplaintActionRequest() {}

    public ComplaintActionRequest(String status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
