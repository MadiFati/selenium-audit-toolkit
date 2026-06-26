package com.example.analyzer;

import com.example.model.AuditIssue;
import com.example.model.AuditReport;
import com.example.rules.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Walks a project directory, parses every .java file with JavaParser,
 * and runs all registered rules against the AST.
 */
public class ProjectAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(ProjectAnalyzer.class);

    private final List<AuditRule> rules = List.of(
        new ThreadSleepRule(),
        new FragileXPathRule(),
        new MissingPageObjectRule(),
        new DirectDriverInstantiationRule(),
        new MissingAssertionRule()
    );

    /**
     * Scan all .java files under the given path and return the full report.
     */
    public AuditReport analyze(Path projectPath) throws IOException {
        AuditReport report = new AuditReport(projectPath.toAbsolutePath().toString());

        log.info("Scanning project: {}", projectPath);

        Files.walk(projectPath)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(javaFile -> analyzeFile(javaFile, report));

        log.info("Scan complete: {} files, {} issues found",
            report.getFilesScanned(), report.getTotalIssues());

        return report;
    }

    private void analyzeFile(Path file, AuditReport report) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            String filePath = file.toString();

            for (AuditRule rule : rules) {
                List<AuditIssue> issues = rule.analyze(cu, filePath);
                issues.forEach(report::addIssue);
            }

            report.incrementFilesScanned();
            log.debug("Analyzed: {}", file.getFileName());

        } catch (IOException e) {
            log.warn("Could not parse file: {} — {}", file, e.getMessage());
        }
    }
}
