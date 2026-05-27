package com.example.backend.user.dto;

import com.example.backend.user.entity.CreditRecord;

import java.time.LocalDateTime;

public class CreditRecordDto {

    private Long id;
    private Long userId;
    private Integer changeAmount;
    private String reason;
    private Long relatedOrderId;
    private LocalDateTime createdAt;

    public CreditRecordDto() {
    }

    public static CreditRecordDto from(CreditRecord record) {
        if (record == null) {
            return null;
        }
        CreditRecordDto dto = new CreditRecordDto();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setChangeAmount(record.getChangeAmount());
        dto.setReason(record.getReason());
        dto.setRelatedOrderId(record.getRelatedOrderId());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
