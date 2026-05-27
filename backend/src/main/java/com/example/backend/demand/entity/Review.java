package com.example.backend.demand.entity;

import com.example.backend.common.enums.ReviewerRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Review {

    private Long id;
    private Long orderId;
    private Long reviewerId;
    private Long revieweeId;
    private ReviewerRole reviewerRole;
    private Integer rating;
    private String content;
    private List<String> imageUrls = new ArrayList<String>();
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(Long revieweeId) {
        this.revieweeId = revieweeId;
    }

    public ReviewerRole getReviewerRole() {
        return reviewerRole;
    }

    public void setReviewerRole(ReviewerRole reviewerRole) {
        this.reviewerRole = reviewerRole;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<String>() : imageUrls;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
