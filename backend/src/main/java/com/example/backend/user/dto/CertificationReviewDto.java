package com.example.backend.user.dto;

public class CertificationReviewDto {

    private Boolean approve;
    private String reason;

    public CertificationReviewDto() {
    }

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
