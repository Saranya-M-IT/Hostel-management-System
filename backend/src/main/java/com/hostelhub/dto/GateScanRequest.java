package com.hostelhub.dto;

import jakarta.validation.constraints.NotBlank;

public class GateScanRequest {

    @NotBlank(message = "QR Token is required.")
    private String qrToken;

    public GateScanRequest() {}

    public GateScanRequest(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
