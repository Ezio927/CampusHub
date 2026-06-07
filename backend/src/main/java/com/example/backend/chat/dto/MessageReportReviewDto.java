package com.example.backend.chat.dto;

public class MessageReportReviewDto {

    private Boolean approve;
    private String resolution;

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
