package com.example.backend.demand.entity;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.RewardType;

import java.time.LocalDateTime;

public class Demand {

    private Long id;
    private Long publisherId;
    private String title;
    private String description;
    private DemandCategory category;
    private Integer rewardAmount;
    private RewardType rewardType;
    private DemandStatus status = DemandStatus.OPEN;
    private Boolean urgent = Boolean.FALSE;
    private LocalDateTime urgentDeadline;
    private String locationTag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isOpen() {
        return status == DemandStatus.OPEN;
    }

    public void markAsAccepted() {
        this.status = DemandStatus.ACCEPTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = DemandStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = DemandStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DemandCategory getCategory() {
        return category;
    }

    public void setCategory(DemandCategory category) {
        this.category = category;
    }

    public Integer getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Integer rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType;
    }

    public DemandStatus getStatus() {
        return status;
    }

    public void setStatus(DemandStatus status) {
        this.status = status;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    public LocalDateTime getUrgentDeadline() {
        return urgentDeadline;
    }

    public void setUrgentDeadline(LocalDateTime urgentDeadline) {
        this.urgentDeadline = urgentDeadline;
    }

    public String getLocationTag() {
        return locationTag;
    }

    public void setLocationTag(String locationTag) {
        this.locationTag = locationTag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
