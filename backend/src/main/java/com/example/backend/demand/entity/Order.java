package com.example.backend.demand.entity;

import com.example.backend.common.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity(name = "DemandOrder")
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "demand_id", nullable = false, unique = true)
    private Long demandId;

    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    @Column(name = "acceptor_id", nullable = false)
    private Long acceptorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ACCEPTED;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "auto_confirm_deadline", nullable = false)
    private LocalDateTime autoConfirmDeadline;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (acceptedAt == null) {
            acceptedAt = now;
        }
        if (autoConfirmDeadline == null) {
            autoConfirmDeadline = acceptedAt.plusHours(24);
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isParticipant(Long userId) {
        return userId != null && (userId.equals(publisherId) || userId.equals(acceptorId));
    }

    public boolean isPublisher(Long userId) {
        return userId != null && userId.equals(publisherId);
    }

    public boolean isAcceptor(Long userId) {
        return userId != null && userId.equals(acceptorId);
    }

    public boolean isAutoConfirmExpired(LocalDateTime now) {
        return status == OrderStatus.PENDING_CONFIRM && !autoConfirmDeadline.isAfter(now);
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
