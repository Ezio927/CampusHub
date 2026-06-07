package com.example.backend.user.dto;

import com.example.backend.common.enums.CertStatus;
import com.example.backend.common.enums.CertType;
import com.example.backend.common.enums.UserGender;
import com.example.backend.user.entity.Certification;

import java.time.LocalDateTime;

public class CertificationDto {

    private Long id;
    private Long userId;
    private CertType certType;
    private String studentId;
    private UserGender gender;
    private String cardImageUrl;
    private CertStatus status;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public CertificationDto() {
    }

    public static CertificationDto from(Certification certification) {
        if (certification == null) {
            return null;
        }
        CertificationDto dto = new CertificationDto();
        dto.setId(certification.getId());
        dto.setUserId(certification.getUserId());
        dto.setCertType(certification.getCertType());
        dto.setStudentId(certification.getStudentId());
        dto.setGender(certification.getGender());
        dto.setCardImageUrl(certification.getCardImageUrl());
        dto.setStatus(certification.getStatus());
        dto.setRejectReason(certification.getRejectReason());
        dto.setReviewedBy(certification.getReviewedBy());
        dto.setReviewedAt(certification.getReviewedAt());
        dto.setCreatedAt(certification.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CertType getCertType() {
        return certType;
    }

    public void setCertType(CertType certType) {
        this.certType = certType;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public CertStatus getStatus() {
        return status;
    }

    public void setStatus(CertStatus status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
