package com.example.backend.demand.dto;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.RewardType;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.DemandImage;
import com.example.backend.demand.entity.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandDto {

    private Long id;
    private Long publisherId;
    private UserPublicDto publisher;
    private String title;
    private String description;
    private DemandCategory category;
    private Integer rewardAmount;
    private RewardType rewardType;
    private DemandStatus status;
    private Boolean urgent;
    private LocalDateTime urgentDeadline;
    private String locationTag;
    private List<Tag> tags = new ArrayList<Tag>();
    private List<DemandImage> images = new ArrayList<DemandImage>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DemandDto from(Demand demand) {
        if (demand == null) {
            return null;
        }
        DemandDto dto = new DemandDto();
        dto.setId(demand.getId());
        dto.setPublisherId(demand.getPublisherId());
        dto.setTitle(demand.getTitle());
        dto.setDescription(demand.getDescription());
        dto.setCategory(demand.getCategory());
        dto.setRewardAmount(demand.getRewardAmount());
        dto.setRewardType(demand.getRewardType());
        dto.setStatus(demand.getStatus());
        dto.setUrgent(demand.getUrgent());
        dto.setUrgentDeadline(demand.getUrgentDeadline());
        dto.setLocationTag(demand.getLocationTag());
        dto.setCreatedAt(demand.getCreatedAt());
        dto.setUpdatedAt(demand.getUpdatedAt());
        return dto;
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

    public UserPublicDto getPublisher() {
        return publisher;
    }

    public void setPublisher(UserPublicDto publisher) {
        this.publisher = publisher;
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags == null ? new ArrayList<Tag>() : tags;
    }

    public List<DemandImage> getImages() {
        return images;
    }

    public void setImages(List<DemandImage> images) {
        this.images = images == null ? new ArrayList<DemandImage>() : images;
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
