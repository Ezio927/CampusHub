package com.example.backend.user.dto;

import com.example.backend.common.enums.QuotaRecordType;
import com.example.backend.user.entity.QuotaRecord;

import java.time.LocalDateTime;

public class QuotaRecordDto {

    private Long id;
    private Long userId;
    private QuotaRecordType type;
    private Integer changeAmount;
    private Integer availableAfter;
    private Integer frozenAfter;
    private Long relatedDemandId;
    private Long relatedOrderId;
    private Long operatorId;
    private String reason;
    private LocalDateTime createdAt;

    public static QuotaRecordDto from(QuotaRecord record) {
        if (record == null) {
            return null;
        }
        QuotaRecordDto dto = new QuotaRecordDto();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setType(record.getType());
        dto.setChangeAmount(record.getChangeAmount());
        dto.setAvailableAfter(record.getAvailableAfter());
        dto.setFrozenAfter(record.getFrozenAfter());
        dto.setRelatedDemandId(record.getRelatedDemandId());
        dto.setRelatedOrderId(record.getRelatedOrderId());
        dto.setOperatorId(record.getOperatorId());
        dto.setReason(record.getReason());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public QuotaRecordType getType() { return type; }
    public void setType(QuotaRecordType type) { this.type = type; }
    public Integer getChangeAmount() { return changeAmount; }
    public void setChangeAmount(Integer changeAmount) { this.changeAmount = changeAmount; }
    public Integer getAvailableAfter() { return availableAfter; }
    public void setAvailableAfter(Integer availableAfter) { this.availableAfter = availableAfter; }
    public Integer getFrozenAfter() { return frozenAfter; }
    public void setFrozenAfter(Integer frozenAfter) { this.frozenAfter = frozenAfter; }
    public Long getRelatedDemandId() { return relatedDemandId; }
    public void setRelatedDemandId(Long relatedDemandId) { this.relatedDemandId = relatedDemandId; }
    public Long getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(Long relatedOrderId) { this.relatedOrderId = relatedOrderId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
