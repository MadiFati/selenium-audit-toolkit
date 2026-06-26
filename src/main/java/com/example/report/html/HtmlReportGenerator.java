package com.example.report.html;

import com.example.model.AuditIssue;
import com.example.model.AuditReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates a self-contained HTML audit report.
 * No external dependencies — all CSS/JS is inlined.
 */
public class HtmlReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(HtmlReportGenerator.class);

    public void generate(AuditReport report, Path outputPath) throws IOException {
        String html = buildHtml(report);
        Files.writeString(outputPath, html);
        log.info("HTML report written: {}", outputPath);
    }

    private String buildHtml(AuditReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Selenium Audit Report</title>
            <style>
              * { box-sizing: border-box; margin: 0; padding: 0; }
              body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                     background: #f5f5f5; color: #333; }
              .header { background: #1a1a2e; color: white; padding: 2rem; }
              .header h1 { font-size: 1.8rem; margin-bottom: 0.5rem; }
              .header p { color: #aaa; font-size: 0.9rem; }
              .container { max-width: 1100px; margin: 2rem auto; padding: 0 1rem; }
              .score-card { background: white; border-radius: 12px; padding: 2rem;
                            margin-bottom: 1.5rem; display: flex; align-items: center;
                            gap: 2rem; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
              .score-circle { width: 100px; height: 100px; border-radius: 50%;
                              display: flex; align-items: center; justify-content: center;
                              font-size: 2rem; font-weight: 700; color: white; flex-shrink: 0; }
              .score-good    { background: #22c55e; }
              .score-warning { background: #f59e0b; }
              .score-bad     { background: #ef4444; }
              .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px,1fr));
                       gap: 1rem; margin-bottom: 1.5rem; }
              .stat { background: white; border-radius: 10px; padding: 1.2rem;
                      text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
              .stat-num { font-size: 2rem; font-weight: 700; }
              .stat-label { font-size: 0.8rem; color: #888; margin-top: 0.3rem; }
              .critical-num { color: #ef4444; }
              .warning-num  { color: #f59e0b; }
              .info-num     { color: #3b82f6; }
              .issues { background: white; border-radius: 12px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden; }
              .issues-header { padding: 1.2rem 1.5rem; border-bottom: 1px solid #eee;
                               font-weight: 600; font-size: 1.1rem; }
              .issue { padding: 1rem 1.5rem; border-bottom: 1px solid #f0f0f0;
                       display: grid; grid-template-columns: 90px 1fr; gap: 1rem;
                       align-items: start; }
              .issue:last-child { border-bottom: none; }
              .badge { display: inline-block; padding: 3px 10px; border-radius: 20px;
                       font-size: 0.72rem; font-weight: 600; text-transform: uppercase; }
              .badge-CRITICAL { background: #fee2e2; color: #b91c1c; }
              .badge-WARNING  { background: #fef3c7; color: #92400e; }
              .badge-INFO     { background: #dbeafe; color: #1e40af; }
              .issue-desc { font-weight: 500; margin-bottom: 0.3rem; }
              .issue-file { font-size: 0.8rem; color: #888; font-family: monospace;
                            margin-bottom: 0.4rem; }
              .issue-rec  { font-size: 0.82rem; color: #555; background: #f8f9fa;
                            padding: 0.5rem 0.8rem; border-radius: 6px;
                            border-left: 3px solid #3b82f6; }
              .no-issues { padding: 3rem; text-align: center; color: #22c55e;
                           font-size: 1.2rem; }
            </style>
            </head>
            <body>
            """);

        // Header
        sb.append("<div class=\"header\">");
        sb.append("<h1>🔍 Selenium Audit Report</h1>");
        sb.append("<p>Project: ").append(report.getProjectPath()).append("</p>");
        sb.append("<p>Generated: ").append(report.getGeneratedAt()).append("</p>");
        sb.append("</div>\n");

        sb.append("<div class=\"container\">\n");

        // Score
        String scoreClass = report.getScore() >= 80 ? "score-good"
            : report.getScore() >= 60 ? "score-warning" : "score-bad";
        sb.append("<div class=\"score-card\">");
        sb.append("<div class=\"score-circle ").append(scoreClass).append("\">")
          .append(report.getScore()).append("</div>");
        sb.append("<div><h2>").append(report.getScoreLabel()).append("</h2>");
        sb.append("<p style=\"color:#888;margin-top:0.4rem\">")
          .append(report.getFilesScanned()).append(" files scanned · ")
          .append(report.getTotalIssues()).append(" issues found</p></div>");
        sb.append("</div>\n");

        // Stats
        sb.append("<div class=\"stats\">");
        sb.append(statCard(report.getFilesScanned(), "Files scanned", ""));
        sb.append(statCard((int)report.getCriticalCount(), "Critical", "critical-num"));
        sb.append(statCard((int)report.getWarningCount(), "Warnings", "warning-num"));
        sb.append(statCard((int)report.getInfoCount(), "Info", "info-num"));
        sb.append("</div>\n");

        // Issues list
        sb.append("<div class=\"issues\">");
        sb.append("<div class=\"issues-header\">Issues found</div>");

        List<AuditIssue> issues = report.getIssues();
        if (issues.isEmpty()) {
            sb.append("<div class=\"no-issues\">✅ No issues found — great job!</div>");
        } else {
            // Sort: CRITICAL first
            issues.stream()
                .sorted((a, b) -> a.getSeverity().compareTo(b.getSeverity()))
                .forEach(issue -> {
                    sb.append("<div class=\"issue\">");
                    sb.append("<div><span class=\"badge badge-")
                      .append(issue.getSeverity()).append("\">")
                      .append(issue.getSeverity()).append("</span></div>");
                    sb.append("<div>");
                    sb.append("<div class=\"issue-desc\">").append(issue.getDescription()).append("</div>");
                    sb.append("<div class=\"issue-file\">")
                      .append(issue.getFile()).append(":").append(issue.getLine())
                      .append("</div>");
                    sb.append("<div class=\"issue-rec\">💡 ").append(issue.getRecommendation()).append("</div>");
                    sb.append("</div></div>\n");
                });
        }
        sb.append("</div>\n");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String statCard(int value, String label, String cssClass) {
        return "<div class=\"stat\"><div class=\"stat-num " + cssClass + "\">" + value +
               "</div><div class=\"stat-label\">" + label + "</div></div>";
    }
}
