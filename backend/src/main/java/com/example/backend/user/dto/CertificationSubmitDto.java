package com.example.backend.user.dto;

import com.example.backend.common.enums.CertType;
import com.example.backend.common.enums.UserGender;

public class CertificationSubmitDto {

    private CertType certType;
    private String studentId;
    private String realName;
    private UserGender gender;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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
}
