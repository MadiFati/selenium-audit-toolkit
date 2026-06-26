package com.example.rules;

import com.example.model.AuditIssue;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CRITICAL — Detects direct WebDriver instantiation in test classes.
 *
 * Why it's bad: creating drivers directly (new ChromeDriver()) prevents
 * parallel execution and makes browser management inconsistent.
 * Fix: use a centralized DriverManager with ThreadLocal for thread-safety.
 */
public class DirectDriverInstantiationRule implements AuditRule {

    private static final Set<String> DRIVER_CLASSES = Set.of(
        "ChromeDriver", "FirefoxDriver", "EdgeDriver",
        "SafariDriver", "RemoteWebDriver"
    );

    @Override
    public String getId() { return "DIRECT_DRIVER"; }

    @Override
    public String getName() { return "Direct WebDriver instantiation"; }

    @Override
    public List<AuditIssue> analyze(CompilationUnit cu, String filePath) {
        List<AuditIssue> issues = new ArrayList<>();

        cu.findAll(ObjectCreationExpr.class).stream()
            .filter(o -> DRIVER_CLASSES.contains(o.getTypeAsString()))
            .forEach(o -> {
                int line = o.getBegin().map(p -> p.line).orElse(0);
                issues.add(new AuditIssue(
                    filePath, line,
                    AuditIssue.Severity.CRITICAL,
                    getId(),
                    "new " + o.getTypeAsString() + "() — direct driver instantiation",
                    "Use a centralized DriverManager with ThreadLocal<WebDriver> for thread-safe parallel execution"
                ));
            });

        return issues;
    }
}
