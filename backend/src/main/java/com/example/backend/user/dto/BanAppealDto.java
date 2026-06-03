package com.example.backend.user.dto;

import com.example.backend.common.enums.CertStatus;
import com.example.backend.user.entity.BanAppeal;

import java.time.LocalDateTime;

public class BanAppealDto {

    private Long id;
    private Long userId;
    private String reason;
    private CertStatus status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public static BanAppealDto from(BanAppeal appeal) {
        if (appeal == null) return null;
        BanAppealDto dto = new BanAppealDto();
        dto.setId(appeal.getId());
        dto.setUserId(appeal.getUserId());
        dto.setReason(appeal.getReason());
        dto.setStatus(appeal.getStatus());
        dto.setReviewedBy(appeal.getReviewedBy());
        dto.setReviewedAt(appeal.getReviewedAt());
        dto.setCreatedAt(appeal.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public CertStatus getStatus() { return status; }
    public void setStatus(CertStatus status) { this.status = status; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
