package com.example.backend.user.dto;

import com.example.backend.common.enums.CreditRecordType;
import com.example.backend.user.entity.CreditRecord;

import java.time.LocalDateTime;

public class CreditRecordDto {

    private Long id;
    private Long userId;
    private CreditRecordType type;
    private Integer changeAmount;
    private Integer beforeScore;
    private Integer afterScore;
    private String reason;
    private Long relatedOrderId;
    private Long operatorId;
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
        dto.setType(record.getType());
        dto.setChangeAmount(record.getChangeAmount());
        dto.setBeforeScore(record.getBeforeScore());
        dto.setAfterScore(record.getAfterScore());
        dto.setReason(record.getReason());
        dto.setRelatedOrderId(record.getRelatedOrderId());
        dto.setOperatorId(record.getOperatorId());
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

    public CreditRecordType getType() {
        return type;
    }

    public void setType(CreditRecordType type) {
        this.type = type;
    }

    public Integer getBeforeScore() {
        return beforeScore;
    }

    public void setBeforeScore(Integer beforeScore) {
        this.beforeScore = beforeScore;
    }

    public Integer getAfterScore() {
        return afterScore;
    }

    public void setAfterScore(Integer afterScore) {
        this.afterScore = afterScore;
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

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
