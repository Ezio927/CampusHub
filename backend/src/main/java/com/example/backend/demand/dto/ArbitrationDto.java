package com.example.backend.demand.dto;

import com.example.backend.common.enums.ArbitrationStatus;
import com.example.backend.demand.entity.ArbitrationTicket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArbitrationDto {

    private Long id;
    private Long reviewId;
    private Long orderId;
    private Long reporterId;
    private String reason;
    private List<String> evidenceUrls = new ArrayList<String>();
    private ArbitrationStatus status;
    private String resolution;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;

    public static ArbitrationDto from(ArbitrationTicket ticket) {
        if (ticket == null) {
            return null;
        }
        ArbitrationDto dto = new ArbitrationDto();
        dto.setId(ticket.getId());
        dto.setReviewId(ticket.getReviewId());
        dto.setOrderId(ticket.getOrderId());
        dto.setReporterId(ticket.getReporterId());
        dto.setReason(ticket.getReason());
        dto.setEvidenceUrls(ticket.getEvidenceUrls());
        dto.setStatus(ticket.getStatus());
        dto.setResolution(ticket.getResolution());
        dto.setResolvedBy(ticket.getResolvedBy());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setCreatedAt(ticket.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls == null ? new ArrayList<String>() : evidenceUrls;
    }

    public ArbitrationStatus getStatus() {
        return status;
    }

    public void setStatus(ArbitrationStatus status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
