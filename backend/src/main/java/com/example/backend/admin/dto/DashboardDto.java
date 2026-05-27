package com.example.backend.admin.dto;

public class DashboardDto {

    private long totalUsers;
    private long activeDemands;
    private long completedOrders;
    private double averageRating;
    private long pendingArbitrations;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveDemands() {
        return activeDemands;
    }

    public void setActiveDemands(long activeDemands) {
        this.activeDemands = activeDemands;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getPendingArbitrations() {
        return pendingArbitrations;
    }

    public void setPendingArbitrations(long pendingArbitrations) {
        this.pendingArbitrations = pendingArbitrations;
    }
}
