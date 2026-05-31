package com.example.backend.chat.dto;

import com.example.backend.common.enums.ReportCategory;

import java.util.ArrayList;
import java.util.List;

public class MessageReportCreateDto {

    private ReportCategory category;
    private String reason;
    private List<String> evidenceUrls = new ArrayList<String>();

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
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
}
