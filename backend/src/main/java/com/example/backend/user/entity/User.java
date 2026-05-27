package com.example.backend.user.entity;

import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String username;
    private String passwordHash;
    private String phone;
    private String email;
    private String studentId;
    private String realName;
    private String avatarUrl;
    private Integer creditScore = 100;
    private UserRole role = UserRole.STUDENT;
    private UserStatus status = UserStatus.PENDING_CERT;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public boolean isCreditSufficient() {
        return getCreditScore() >= 60;
    }

    public void addCredit(int amount) {
        if (amount <= 0) {
            return;
        }
        this.creditScore = Math.max(0, getCreditScore() + amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void deductCredit(int amount) {
        if (amount <= 0) {
            return;
        }
        this.creditScore = Math.max(0, getCreditScore() - amount);
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getCreditScore() {
        return creditScore == null ? 0 : creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
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
