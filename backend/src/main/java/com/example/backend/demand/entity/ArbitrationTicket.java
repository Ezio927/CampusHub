package com.example.backend.demand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.backend.common.enums.ArbitrationStatus;
import com.example.backend.common.mybatis.StringListTypeHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TableName("arbitration_tickets")
public class ArbitrationTicket {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reviewId;
    private Long orderId;
    private Long reporterId;
    private String reason;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> evidenceUrls = new ArrayList<>();

    private ArbitrationStatus status = ArbitrationStatus.PENDING;
    private String resolution;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<String> getEvidenceUrls() { return evidenceUrls; }
    public void setEvidenceUrls(List<String> evidenceUrls) { this.evidenceUrls = evidenceUrls == null ? new ArrayList<>() : evidenceUrls; }
    public ArbitrationStatus getStatus() { return status; }
    public void setStatus(ArbitrationStatus status) { this.status = status; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Long getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Long resolvedBy) { this.resolvedBy = resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
