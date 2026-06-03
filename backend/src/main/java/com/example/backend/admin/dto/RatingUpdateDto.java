package com.example.backend.admin.dto;

import com.example.backend.common.enums.ReviewerRole;

public class RatingUpdateDto {

    private ReviewerRole role;
    private Double averageRating;
    private Integer ratingCount;
    private String reason;

    public ReviewerRole getRole() {
        return role;
    }

    public void setRole(ReviewerRole role) {
        this.role = role;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
