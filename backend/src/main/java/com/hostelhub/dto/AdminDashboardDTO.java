package com.hostelhub.dto;

public class AdminDashboardDTO {

    private long totalStudents;
    private long totalWardens;
    private long totalRooms;
    private long occupiedRooms;
    private long vacantRooms;
    private long totalCapacity;
    private long allocatedStudents;
    private long unallocatedStudents;
    private long pendingLeaves;
    private long pendingComplaints;
    private int occupancyRate;

    public AdminDashboardDTO() {}

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalWardens() {
        return totalWardens;
    }

    public void setTotalWardens(long totalWardens) {
        this.totalWardens = totalWardens;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public long getOccupiedRooms() {
        return occupiedRooms;
    }

    public void setOccupiedRooms(long occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public long getVacantRooms() {
        return vacantRooms;
    }

    public void setVacantRooms(long vacantRooms) {
        this.vacantRooms = vacantRooms;
    }

    public long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public long getAllocatedStudents() {
        return allocatedStudents;
    }

    public void setAllocatedStudents(long allocatedStudents) {
        this.allocatedStudents = allocatedStudents;
    }

    public long getUnallocatedStudents() {
        return unallocatedStudents;
    }

    public void setUnallocatedStudents(long unallocatedStudents) {
        this.unallocatedStudents = unallocatedStudents;
    }

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

    public int getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(int occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
}
