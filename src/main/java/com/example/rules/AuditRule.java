package com.example.rules;

import com.example.model.AuditIssue;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

/**
 * Contract for all audit rules.
 * Each rule analyses a parsed Java file and returns a list of issues found.
 */
public interface AuditRule {

    /** Unique rule identifier (e.g. "THREAD_SLEEP") */
    String getId();

    /** Human-readable name */
    String getName();

    /** Analyse the AST and return all issues found */
    List<AuditIssue> analyze(CompilationUnit cu, String filePath);
}
