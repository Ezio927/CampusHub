package com.example.backend.user.dto;

import com.example.backend.common.enums.CertType;

public class CertificationSubmitDto {

    private CertType certType;
    private String studentId;
    private String cardImageUrl;

    public CertificationSubmitDto() {
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

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }
}
