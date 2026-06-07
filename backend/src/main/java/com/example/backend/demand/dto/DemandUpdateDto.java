package com.example.backend.demand.dto;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.RewardType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandUpdateDto {

    private String title;
    private String description;
    private DemandCategory category;
    private Integer rewardAmount;
    private RewardType rewardType;
    private Boolean urgent;
    private LocalDateTime urgentDeadline;
    private String locationTag;
    private String campus;
    private List<Long> tagIds = new ArrayList<Long>();
    private List<String> imageUrls = new ArrayList<String>();

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

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds == null ? new ArrayList<Long>() : tagIds;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<String>() : imageUrls;
    }
}
