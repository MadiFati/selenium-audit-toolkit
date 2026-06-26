package com.example.cli;

import com.example.analyzer.ProjectAnalyzer;
import com.example.model.AuditReport;
import com.example.report.html.HtmlReportGenerator;
import com.example.report.json.JsonReportGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Main CLI entry point.
 *
 * Usage:
 *   java -jar selenium-audit-toolkit.jar /path/to/project
 *   java -jar selenium-audit-toolkit.jar /path/to/project --format html --output report.html
 *   java -jar selenium-audit-toolkit.jar /path/to/project --format json --output report.json
 *   java -jar selenium-audit-toolkit.jar /path/to/project --format both
 */
@Command(
    name = "selenium-audit",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Audits a Selenium Java project and detects bad practices"
)
public class AuditCli implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the Selenium project to audit")
    private Path projectPath;

    @Option(names = {"-f", "--format"},
            description = "Output format: html, json, both (default: both)",
            defaultValue = "both")
    private String format;

    @Option(names = {"-o", "--output"},
            description = "Output file name (without extension, default: audit-report)",
            defaultValue = "audit-report")
    private String outputName;

    @Option(names = {"--fail-on-critical"},
            description = "Exit with code 1 if critical issues are found (useful in CI)")
    private boolean failOnCritical;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AuditCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("\n🔍 Selenium Audit Toolkit v1.0.0");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Project : " + projectPath.toAbsolutePath());
        System.out.println("Format  : " + format);
        System.out.println();

        // Run analysis
        ProjectAnalyzer analyzer = new ProjectAnalyzer();
        AuditReport report = analyzer.analyze(projectPath);

        // Print summary to console
        printSummary(report);

        // Generate reports
        if (format.equals("html") || format.equals("both")) {
            Path htmlPath = Path.of(outputName + ".html");
            new HtmlReportGenerator().generate(report, htmlPath);
            System.out.println("📄 HTML report : " + htmlPath.toAbsolutePath());
        }

        if (format.equals("json") || format.equals("both")) {
            Path jsonPath = Path.of(outputName + ".json");
            new JsonReportGenerator().generate(report, jsonPath);
            System.out.println("📋 JSON report : " + jsonPath.toAbsolutePath());
        }

        System.out.println("\nScore : " + report.getScore() + "/100 — " + report.getScoreLabel());

        // CI fail gate
        if (failOnCritical && report.getCriticalCount() > 0) {
            System.err.println("\n❌ " + report.getCriticalCount() + " critical issue(s) found — failing build");
            return 1;
        }

        return 0;
    }

    private void printSummary(AuditReport report) {
        System.out.printf("Files scanned : %d%n", report.getFilesScanned());
        System.out.printf("Issues found  : %d (🔴 %d critical, 🟡 %d warning, 🔵 %d info)%n%n",
            report.getTotalIssues(),
            report.getCriticalCount(),
            report.getWarningCount(),
            report.getInfoCount());

        if (!report.getIssues().isEmpty()) {
            System.out.println("Top issues:");
            report.getIssues().stream()
                .limit(5)
                .forEach(i -> System.out.printf("  [%s] %s (line %d)%n",
                    i.getSeverity(), i.getDescription(), i.getLine()));
            if (report.getTotalIssues() > 5) {
                System.out.printf("  ... and %d more (see report)%n", report.getTotalIssues() - 5);
            }
            System.out.println();
        }
    }
}
