package com.example.model;

/**
 * Represents a single bad practice detected in a Selenium project.
 */
public class AuditIssue {

    public enum Severity { CRITICAL, WARNING, INFO }

    private final String file;
    private final int line;
    private final Severity severity;
    private final String rule;
    private final String description;
    private final String recommendation;

    public AuditIssue(String file, int line, Severity severity,
                      String rule, String description, String recommendation) {
        this.file           = file;
        this.line           = line;
        this.severity       = severity;
        this.rule           = rule;
        this.description    = description;
        this.recommendation = recommendation;
    }

    public String getFile()             { return file; }
    public int getLine()                { return line; }
    public Severity getSeverity()       { return severity; }
    public String getRule()             { return rule; }
    public String getDescription()      { return description; }
    public String getRecommendation()   { return recommendation; }

    @Override
    public String toString() {
        return "[%s] %s:%d — %s".formatted(severity, file, line, description);
    }
}
