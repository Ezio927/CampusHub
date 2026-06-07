package com.example.backend.demand.dto;

import java.util.ArrayList;
import java.util.List;

public class ArbitrationCreateDto {

    private Long orderId;
    private Long reviewId;
    private String reason;
    private List<String> evidenceUrls = new ArrayList<String>();

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls == null ? new ArrayList<String>() : evidenceUrls;
    }
}
