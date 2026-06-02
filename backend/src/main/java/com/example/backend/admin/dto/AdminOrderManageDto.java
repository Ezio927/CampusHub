package com.example.backend.admin.dto;

import java.time.LocalDateTime;

public class AdminOrderManageDto {

    private String rowType;
    private Long orderId;
    private Long demandId;
    private String demandTitle;
    private Long publisherId;
    private String publisherName;
    private Long acceptorId;
    private String acceptorName;
    private String status;
    private LocalDateTime createdAt;

    public String getRowType() {
        return rowType;
    }

    public void setRowType(String rowType) {
        this.rowType = rowType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public String getDemandTitle() {
        return demandTitle;
    }

    public void setDemandTitle(String demandTitle) {
        this.demandTitle = demandTitle;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public Long getAcceptorId() {
        return acceptorId;
    }

    public void setAcceptorId(Long acceptorId) {
        this.acceptorId = acceptorId;
    }

    public String getAcceptorName() {
        return acceptorName;
    }

    public void setAcceptorName(String acceptorName) {
        this.acceptorName = acceptorName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
