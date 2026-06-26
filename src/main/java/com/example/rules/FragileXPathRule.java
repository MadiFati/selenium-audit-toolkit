package com.example.rules;

import com.example.model.AuditIssue;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * WARNING — Detects fragile XPath locators.
 *
 * Why it's bad: XPaths like //div[3]/span[2] break when the UI changes slightly.
 * Fix: use By.id(), By.cssSelector() with meaningful attributes, or data-testid.
 */
public class FragileXPathRule implements AuditRule {

    // Patterns indicating fragile XPath: positional, contains(text(), ...), absolute paths
    private static final Pattern FRAGILE_XPATH = Pattern.compile(
        "(//\\w+\\[\\d+\\])" +           // positional: //div[3]
        "|(contains\\(text\\(\\))" +       // text() is brittle
        "|(^/html/body)"                   // absolute path from root
    );

    @Override
    public String getId() { return "FRAGILE_XPATH"; }

    @Override
    public String getName() { return "Fragile XPath locator"; }

    @Override
    public List<AuditIssue> analyze(CompilationUnit cu, String filePath) {
        List<AuditIssue> issues = new ArrayList<>();

        cu.findAll(MethodCallExpr.class).stream()
            .filter(m -> m.getNameAsString().equals("xpath"))
            .forEach(m -> m.getArguments().forEach(arg -> {
                if (arg instanceof StringLiteralExpr str) {
                    String xpath = str.asString();
                    if (FRAGILE_XPATH.matcher(xpath).find()) {
                        int line = m.getBegin().map(p -> p.line).orElse(0);
                        issues.add(new AuditIssue(
                            filePath, line,
                            AuditIssue.Severity.WARNING,
                            getId(),
                            "Fragile XPath: \"" + truncate(xpath, 60) + "\"",
                            "Use By.id(), By.cssSelector('[data-testid=\"...\"]') or a stable attribute"
                        ));
                    }
                }
            }));

        return issues;
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
