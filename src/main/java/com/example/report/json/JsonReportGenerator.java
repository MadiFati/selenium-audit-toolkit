package com.example.report.json;

import com.example.model.AuditReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Generates a JSON audit report — integrable in CI pipelines.
 */
public class JsonReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(JsonReportGenerator.class);
    private final ObjectMapper mapper;

    public JsonReportGenerator() {
        this.mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void generate(AuditReport report, Path outputPath) throws IOException {
        mapper.writeValue(outputPath.toFile(), buildJsonModel(report));
        log.info("JSON report written: {}", outputPath);
    }

    public String generateString(AuditReport report) throws IOException {
        return mapper.writeValueAsString(buildJsonModel(report));
    }

    private Object buildJsonModel(AuditReport report) {
        return new java.util.LinkedHashMap<>() {{
            put("generatedAt",   report.getGeneratedAt());
            put("projectPath",   report.getProjectPath());
            put("filesScanned",  report.getFilesScanned());
            put("score",         report.getScore());
            put("scoreLabel",    report.getScoreLabel());
            put("summary", new java.util.LinkedHashMap<>() {{
                put("total",    report.getTotalIssues());
                put("critical", report.getCriticalCount());
                put("warning",  report.getWarningCount());
                put("info",     report.getInfoCount());
            }});
            put("issues", report.getIssues().stream().map(issue ->
                new java.util.LinkedHashMap<>() {{
                    put("severity",       issue.getSeverity().name());
                    put("rule",           issue.getRule());
                    put("file",           issue.getFile());
                    put("line",           issue.getLine());
                    put("description",    issue.getDescription());
                    put("recommendation", issue.getRecommendation());
                }}
            ).toList());
        }};
    }
}
