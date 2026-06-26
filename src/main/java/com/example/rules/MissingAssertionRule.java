package com.example.rules;

import com.example.model.AuditIssue;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * WARNING — Detects test methods with no assertions.
 *
 * Why it's bad: a test without assertions always passes, even if the app is broken.
 * It gives false confidence in the test suite.
 * Fix: add at least one assertTrue/assertEquals/assertThat per test method.
 */
public class MissingAssertionRule implements AuditRule {

    @Override
    public String getId() { return "MISSING_ASSERTION"; }

    @Override
    public String getName() { return "Test method without assertion"; }

    @Override
    public List<AuditIssue> analyze(CompilationUnit cu, String filePath) {
        List<AuditIssue> issues = new ArrayList<>();

        cu.findAll(MethodDeclaration.class).stream()
            .filter(this::isTestMethod)
            .forEach(method -> {
                boolean hasAssertion = method.findAll(MethodCallExpr.class).stream()
                    .anyMatch(m -> m.getNameAsString().startsWith("assert")
                        || m.getNameAsString().startsWith("verify")
                        || m.getNameAsString().equals("fail"));

                if (!hasAssertion) {
                    int line = method.getBegin().map(p -> p.line).orElse(0);
                    issues.add(new AuditIssue(
                        filePath, line,
                        AuditIssue.Severity.WARNING,
                        getId(),
                        "Test method '" + method.getNameAsString() + "' has no assertion — always passes",
                        "Add at least one assertion: assertTrue(), assertEquals(), or assertThat()"
                    ));
                }
            });

        return issues;
    }

    private boolean isTestMethod(MethodDeclaration method) {
        return method.getAnnotations().stream()
            .anyMatch(a -> a.getNameAsString().equals("Test"));
    }
}
