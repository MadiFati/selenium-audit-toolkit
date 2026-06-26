package com.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates all issues found during an audit scan.
 */
public class AuditReport {

    private final String projectPath;
    private final String generatedAt;
    private final List<AuditIssue> issues = new ArrayList<>();
    private int filesScanned = 0;

    public AuditReport(String projectPath) {
        this.projectPath = projectPath;
        this.generatedAt = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public void addIssue(AuditIssue issue) {
        issues.add(issue);
    }

    public void incrementFilesScanned() {
        filesScanned++;
    }

    // ── Computed stats ────────────────────────────────────────────────────────

    public long getCriticalCount() {
        return issues.stream()
            .filter(i -> i.getSeverity() == AuditIssue.Severity.CRITICAL)
            .count();
    }

    public long getWarningCount() {
        return issues.stream()
            .filter(i -> i.getSeverity() == AuditIssue.Severity.WARNING)
            .count();
    }

    public long getInfoCount() {
        return issues.stream()
            .filter(i -> i.getSeverity() == AuditIssue.Severity.INFO)
            .count();
    }

    public int getScore() {
        // Score 0-100 : starts at 100, deducted by severity
        int deductions = (int)(getCriticalCount() * 15 + getWarningCount() * 5 + getInfoCount() * 1);
        return Math.max(0, 100 - deductions);
    }

    public String getScoreLabel() {
        int score = getScore();
        if (score >= 80) return "Good";
        if (score >= 60) return "Needs improvement";
        return "Critical";
    }

    public Map<String, Long> getIssuesByRule() {
        return issues.stream()
            .collect(Collectors.groupingBy(AuditIssue::getRule, Collectors.counting()));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getProjectPath()  { return projectPath; }
    public String getGeneratedAt()  { return generatedAt; }
    public List<AuditIssue> getIssues() { return issues; }
    public int getFilesScanned()    { return filesScanned; }
    public int getTotalIssues()     { return issues.size(); }
}
