package com.example.report.json;

import com.example.model.AuditIssue;
import com.example.model.AuditReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, Object> buildJsonModel(AuditReport report) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("generatedAt", report.getGeneratedAt());
        root.put("projectPath", report.getProjectPath());
        root.put("filesScanned", report.getFilesScanned());
        root.put("score", report.getScore());
        root.put("scoreLabel", report.getScoreLabel());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", report.getTotalIssues());
        summary.put("critical", report.getCriticalCount());
        summary.put("warning", report.getWarningCount());
        summary.put("info", report.getInfoCount());
        root.put("summary", summary);

        List<Map<String, Object>> issuesList = report.getIssues().stream()
            .map(i -> {
                Map<String, Object> issueMap = new LinkedHashMap<>();
                issueMap.put("severity", i.getSeverity().name());
                issueMap.put("rule", i.getRule());
                issueMap.put("file", i.getFile());
                issueMap.put("line", i.getLine());
                issueMap.put("description", i.getDescription());
                issueMap.put("recommendation", i.getRecommendation());
                return issueMap;
            })
            .toList();

        root.put("issues", issuesList);
        return root;
    }
}
