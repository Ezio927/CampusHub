package com.example.backend.demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.backend.common.enums.OrderStatus;

import java.time.LocalDateTime;

@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long publisherId;
    private Long acceptorId;
    private OrderStatus status = OrderStatus.PENDING_ACCEPT;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private LocalDateTime autoConfirmDeadline;
    private Integer acceptorDepositAmount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean canAccept() { return status == OrderStatus.PENDING_ACCEPT; }
    public boolean isParticipant(Long userId) { return userId != null && (userId.equals(publisherId) || userId.equals(acceptorId)); }
    public boolean isPublisher(Long userId) { return userId != null && userId.equals(publisherId); }
    public boolean isAcceptor(Long userId) { return userId != null && userId.equals(acceptorId); }
    public boolean canCancel() { return status == OrderStatus.PENDING_ACCEPT || status == OrderStatus.ACCEPTED || status == OrderStatus.PENDING_CONFIRM; }
    public boolean isAutoConfirmExpired() { return autoConfirmDeadline != null && autoConfirmDeadline.isBefore(LocalDateTime.now()); }
    public boolean isAutoConfirmExpired(LocalDateTime now) { return autoConfirmDeadline != null && now != null && autoConfirmDeadline.isBefore(now); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public Long getAcceptorId() { return acceptorId; }
    public void setAcceptorId(Long acceptorId) { this.acceptorId = acceptorId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getAutoConfirmDeadline() { return autoConfirmDeadline; }
    public void setAutoConfirmDeadline(LocalDateTime autoConfirmDeadline) { this.autoConfirmDeadline = autoConfirmDeadline; }
    public Integer getAcceptorDepositAmount() { return acceptorDepositAmount == null ? 0 : acceptorDepositAmount; }
    public void setAcceptorDepositAmount(Integer acceptorDepositAmount) { this.acceptorDepositAmount = acceptorDepositAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
