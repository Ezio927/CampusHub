package com.example.backend.admin.entity;

import java.time.LocalDateTime;

public class AdminOperationLog {

    private Long id;
    private Long adminId;
    private String operation;
    private String targetType;
    private Long targetId;
    private String detail;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Long getOperatorId() {
        return adminId;
    }

    public void setOperatorId(Long operatorId) {
        this.adminId = operatorId;
    }

    public String getAction() {
        return operation;
    }

    public void setAction(String action) {
        this.operation = action;
    }

    public String getRemark() {
        return detail;
    }

    public void setRemark(String remark) {
        this.detail = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
