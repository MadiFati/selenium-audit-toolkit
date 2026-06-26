package com.example.rules;

import com.example.model.AuditIssue;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * CRITICAL — Detects Thread.sleep() calls.
 *
 * Why it's bad: hard-coded waits make tests slow and flaky.
 * The test passes when the sleep is too long, fails when the app is slower than expected.
 * Fix: use WebDriverWait with ExpectedConditions instead.
 */
public class ThreadSleepRule implements AuditRule {

    @Override
    public String getId() { return "THREAD_SLEEP"; }

    @Override
    public String getName() { return "Thread.sleep() detected"; }

    @Override
    public List<AuditIssue> analyze(CompilationUnit cu, String filePath) {
        List<AuditIssue> issues = new ArrayList<>();

        cu.findAll(MethodCallExpr.class).stream()
            .filter(m -> m.getNameAsString().equals("sleep")
                && m.getScope().map(s -> s.toString().equals("Thread")).orElse(false))
            .forEach(m -> {
                int line = m.getBegin().map(p -> p.line).orElse(0);
                issues.add(new AuditIssue(
                    filePath, line,
                    AuditIssue.Severity.CRITICAL,
                    getId(),
                    "Thread.sleep() found — hard-coded wait causes flaky tests",
                    "Replace with WebDriverWait: new WebDriverWait(driver, Duration.ofSeconds(10))" +
                    ".until(ExpectedConditions.visibilityOf(element))"
                ));
            });

        return issues;
    }
}
