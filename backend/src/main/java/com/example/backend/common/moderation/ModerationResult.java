package com.example.backend.common.moderation;

import com.example.backend.common.enums.ReportCategory;

import java.util.ArrayList;
import java.util.List;

public class ModerationResult {

    private final List<ModerationIssue> issues = new ArrayList<ModerationIssue>();
    private final List<String> warnings = new ArrayList<String>();

    public void addIssue(ModerationIssue issue) {
        if (issue != null) {
            issues.add(issue);
        }
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.trim().isEmpty()) {
            warnings.add(warning);
        }
    }

    public List<ModerationIssue> getIssues() {
        return issues;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean blocksUsername() {
        return !issues.isEmpty();
    }

    public boolean blocksMessage() {
        for (ModerationIssue issue : issues) {
            if (issue.getCategory() == ReportCategory.SEXUAL
                    || issue.getCategory() == ReportCategory.POLITICAL
                    || issue.getCategory() == ReportCategory.LINK_OR_QR) {
                return true;
            }
            if (issue.getCategory() == ReportCategory.ABUSE
                    && issue.getSeverity() == ModerationIssue.Severity.HEAVY) {
                return true;
            }
        }
        return false;
    }
}
