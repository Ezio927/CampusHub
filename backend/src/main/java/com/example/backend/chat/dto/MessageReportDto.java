package com.example.backend.chat.dto;

import com.example.backend.chat.entity.MessageReport;
import com.example.backend.common.enums.MessageType;
import com.example.backend.common.enums.ReportCategory;
import com.example.backend.common.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageReportDto {

    private Long id;
    private Long messageId;
    private Long conversationId;
    private Long reporterId;
    private Long targetUserId;
    private ReportCategory category;
    private String reason;
    private List<String> evidenceUrls = new ArrayList<String>();
    private String messageSnapshot;
    private MessageType messageType;
    private String messageImageUrl;
    private ReportStatus status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String resolution;
    private LocalDateTime createdAt;

    public static MessageReportDto from(MessageReport report) {
        if (report == null) {
            return null;
        }
        MessageReportDto dto = new MessageReportDto();
        dto.setId(report.getId());
        dto.setMessageId(report.getMessageId());
        dto.setConversationId(report.getConversationId());
        dto.setReporterId(report.getReporterId());
        dto.setTargetUserId(report.getTargetUserId());
        dto.setCategory(report.getCategory());
        dto.setReason(report.getReason());
        dto.setEvidenceUrls(report.getEvidenceUrls());
        dto.setMessageSnapshot(report.getMessageSnapshot());
        dto.setMessageType(report.getMessageType());
        dto.setMessageImageUrl(report.getMessageImageUrl());
        dto.setStatus(report.getStatus());
        dto.setReviewedBy(report.getReviewedBy());
        dto.setReviewedAt(report.getReviewedAt());
        dto.setResolution(report.getResolution());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public ReportCategory getCategory() { return category; }
    public void setCategory(ReportCategory category) { this.category = category; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<String> getEvidenceUrls() { return evidenceUrls; }
    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls == null ? new ArrayList<String>() : evidenceUrls;
    }
    public String getMessageSnapshot() { return messageSnapshot; }
    public void setMessageSnapshot(String messageSnapshot) { this.messageSnapshot = messageSnapshot; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public String getMessageImageUrl() { return messageImageUrl; }
    public void setMessageImageUrl(String messageImageUrl) { this.messageImageUrl = messageImageUrl; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
