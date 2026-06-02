package com.example.backend.admin.dto;

import java.time.LocalDateTime;

public class AdminUserDto {

    private Long id;
    private String username;
    private String studentId;
    private String realName;
    private String status;
    private String role;
    private Integer creditScore;
    private Double publisherRating;
    private Integer publisherRatingCount;
    private Double acceptorRating;
    private Integer acceptorRatingCount;
    private Integer quotaAvailable;
    private Integer quotaFrozen;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Double getPublisherRating() {
        return publisherRating;
    }

    public void setPublisherRating(Double publisherRating) {
        this.publisherRating = publisherRating;
    }

    public Integer getPublisherRatingCount() {
        return publisherRatingCount;
    }

    public void setPublisherRatingCount(Integer publisherRatingCount) {
        this.publisherRatingCount = publisherRatingCount;
    }

    public Double getAcceptorRating() {
        return acceptorRating;
    }

    public void setAcceptorRating(Double acceptorRating) {
        this.acceptorRating = acceptorRating;
    }

    public Integer getAcceptorRatingCount() {
        return acceptorRatingCount;
    }

    public void setAcceptorRatingCount(Integer acceptorRatingCount) {
        this.acceptorRatingCount = acceptorRatingCount;
    }

    public Integer getQuotaAvailable() {
        return quotaAvailable;
    }

    public void setQuotaAvailable(Integer quotaAvailable) {
        this.quotaAvailable = quotaAvailable;
    }

    public Integer getQuotaFrozen() {
        return quotaFrozen;
    }

    public void setQuotaFrozen(Integer quotaFrozen) {
        this.quotaFrozen = quotaFrozen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
