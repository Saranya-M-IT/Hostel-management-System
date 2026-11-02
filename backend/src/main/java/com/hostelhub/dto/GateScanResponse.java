package com.hostelhub.dto;

public class GateScanResponse {

    private boolean success;
    private String type; // "IN" or "OUT"
    private String scanTime;
    private String message;
    private GateLogDTO log;

    public GateScanResponse() {}

    public GateScanResponse(boolean success, String type, String scanTime, String message, GateLogDTO log) {
        this.success = success;
        this.type = type;
        this.scanTime = scanTime;
        this.message = message;
        this.log = log;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GateLogDTO getLog() {
        return log;
    }

    public void setLog(GateLogDTO log) {
        this.log = log;
    }
}
