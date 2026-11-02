package com.hostelhub.dto;

public class WardenDashboardDTO {

    private long pendingLeaves;
    private long pendingComplaints;
    private long unallocatedStudents;
    private long vacantRooms;
    private String block;

    public WardenDashboardDTO() {}

    public long getPendingLeaves() {
        return pendingLeaves;
    }

    public void setPendingLeaves(long pendingLeaves) {
        this.pendingLeaves = pendingLeaves;
    }

    public long getPendingComplaints() {
        return pendingComplaints;
    }

    public void setPendingComplaints(long pendingComplaints) {
        this.pendingComplaints = pendingComplaints;
    }

    public long getUnallocatedStudents() {
        return unallocatedStudents;
    }

    public void setUnallocatedStudents(long unallocatedStudents) {
        this.unallocatedStudents = unallocatedStudents;
    }

    public long getVacantRooms() {
        return vacantRooms;
    }

    public void setVacantRooms(long vacantRooms) {
        this.vacantRooms = vacantRooms;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }
}
