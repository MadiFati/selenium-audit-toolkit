package com.example.rules;

import com.example.model.AuditIssue;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * WARNING — Detects direct driver.findElement() calls inside test classes.
 *
 * Why it's bad: mixing navigation logic with assertions makes tests unmaintainable.
 * If a locator changes, you have to update every test instead of one Page Object.
 * Fix: move all findElement() calls to dedicated Page Object classes.
 */
public class MissingPageObjectRule implements AuditRule {

    @Override
    public String getId() { return "MISSING_POM"; }

    @Override
    public String getName() { return "Direct findElement() in test class"; }

    @Override
    public List<AuditIssue> analyze(CompilationUnit cu, String filePath) {
        List<AuditIssue> issues = new ArrayList<>();

        // Only check test files (named *Test.java or *Tests.java)
        String fileName = filePath.contains("/") 
            ? filePath.substring(filePath.lastIndexOf('/') + 1) 
            : filePath;
        
        if (!fileName.matches(".*Tests?\\.java$")) return issues;

        cu.findAll(MethodCallExpr.class).stream()
            .filter(m -> m.getNameAsString().equals("findElement")
                || m.getNameAsString().equals("findElements"))
            .forEach(m -> {
                int line = m.getBegin().map(p -> p.line).orElse(0);
                issues.add(new AuditIssue(
                    filePath, line,
                    AuditIssue.Severity.WARNING,
                    getId(),
                    "Direct " + m.getNameAsString() + "() call in test class — violates Page Object Model",
                    "Move this locator to a Page Object class and expose a meaningful action method"
                ));
            });

        return issues;
    }
}
