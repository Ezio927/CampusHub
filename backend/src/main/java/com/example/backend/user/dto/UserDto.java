package com.example.backend.user.dto;

import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserGender;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.entity.User;

import java.time.LocalDateTime;

public class UserDto {

    private Long id;
    private String username;
    private String realName;
    private String studentId;
    private String phone;
    private String email;
    private String avatarUrl;
    private UserGender gender;
    private Integer creditScore;
    private Double publisherRating;
    private Integer publisherRatingCount;
    private Double acceptorRating;
    private Integer acceptorRatingCount;
    private Integer quotaAvailable;
    private Integer quotaFrozen;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;

    public UserDto() {
    }

    public static UserDto from(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setStudentId(user.getStudentId());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender());
        dto.setCreditScore(user.getCreditScore());
        dto.setPublisherRating(user.getPublisherRating());
        dto.setPublisherRatingCount(user.getPublisherRatingCount());
        dto.setAcceptorRating(user.getAcceptorRating());
        dto.setAcceptorRatingCount(user.getAcceptorRatingCount());
        dto.setQuotaAvailable(user.getQuotaAvailable());
        dto.setQuotaFrozen(user.getQuotaFrozen());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Double getPublisherRating() {
        return publisherRating;
    }

    public void setPublisherRating(Double publisherRating) {
        this.publisherRating = publisherRating;
    }

    public Integer getPublisherRatingCount() {
        return publisherRatingCount;
    }

    public void setPublisherRatingCount(Integer publisherRatingCount) {
        this.publisherRatingCount = publisherRatingCount;
    }

    public Double getAcceptorRating() {
        return acceptorRating;
    }

    public void setAcceptorRating(Double acceptorRating) {
        this.acceptorRating = acceptorRating;
    }

    public Integer getAcceptorRatingCount() {
        return acceptorRatingCount;
    }

    public void setAcceptorRatingCount(Integer acceptorRatingCount) {
        this.acceptorRatingCount = acceptorRatingCount;
    }

    public Integer getQuotaAvailable() {
        return quotaAvailable;
    }

    public void setQuotaAvailable(Integer quotaAvailable) {
        this.quotaAvailable = quotaAvailable;
    }

    public Integer getQuotaFrozen() {
        return quotaFrozen;
    }

    public void setQuotaFrozen(Integer quotaFrozen) {
        this.quotaFrozen = quotaFrozen;
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
}
