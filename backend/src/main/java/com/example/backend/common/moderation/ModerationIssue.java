package com.example.backend.common.moderation;

import com.example.backend.common.enums.ReportCategory;

public class ModerationIssue {

    public enum Severity {
        INFO,
        LIGHT,
        HEAVY
    }

    private final ReportCategory category;
    private final Severity severity;
    private final String message;

    public ModerationIssue(ReportCategory category, Severity severity, String message) {
        this.category = category;
        this.severity = severity;
        this.message = message;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}
