package com.example.backend.demand.dto;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;

import java.time.LocalDateTime;

public class OrderDto {

    private Long id;
    private Long demandId;
    private Long publisherId;
    private Long acceptorId;
    private String demandTitle;
    private String publisherName;
    private String acceptorName;
    private Integer rewardAmount;
    private String rewardType;
    private OrderStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private Integer creditPenalty;
    private Integer quotaPenalty;
    private Integer acceptorDepositAmount;
    private LocalDateTime autoConfirmDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderDto from(Order order) {
        if (order == null) {
            return null;
        }
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setDemandId(order.getDemandId());
        dto.setPublisherId(order.getPublisherId());
        dto.setAcceptorId(order.getAcceptorId());
        dto.setStatus(order.getStatus());
        dto.setAcceptedAt(order.getAcceptedAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCancelReason(order.getCancelReason());
        dto.setAcceptorDepositAmount(order.getAcceptorDepositAmount());
        dto.setAutoConfirmDeadline(order.getAutoConfirmDeadline());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public Long getAcceptorId() {
        return acceptorId;
    }

    public void setAcceptorId(Long acceptorId) {
        this.acceptorId = acceptorId;
    }

    public String getDemandTitle() { return demandTitle; }
    public void setDemandTitle(String demandTitle) { this.demandTitle = demandTitle; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getAcceptorName() { return acceptorName; }
    public void setAcceptorName(String acceptorName) { this.acceptorName = acceptorName; }
    public Integer getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(Integer rewardAmount) { this.rewardAmount = rewardAmount; }
    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Integer getCreditPenalty() {
        return creditPenalty;
    }

    public void setCreditPenalty(Integer creditPenalty) {
        this.creditPenalty = creditPenalty;
    }

    public Integer getQuotaPenalty() {
        return quotaPenalty;
    }

    public void setQuotaPenalty(Integer quotaPenalty) {
        this.quotaPenalty = quotaPenalty;
    }

    public Integer getAcceptorDepositAmount() {
        return acceptorDepositAmount;
    }

    public void setAcceptorDepositAmount(Integer acceptorDepositAmount) {
        this.acceptorDepositAmount = acceptorDepositAmount;
    }

    public LocalDateTime getAutoConfirmDeadline() {
        return autoConfirmDeadline;
    }

    public void setAutoConfirmDeadline(LocalDateTime autoConfirmDeadline) {
        this.autoConfirmDeadline = autoConfirmDeadline;
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
