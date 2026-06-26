# selenium-audit-toolkit

![Build](https://github.com/MadiFati/selenium-audit-toolkit/actions/workflows/build.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![License](https://img.shields.io/badge/license-MIT-blue)

CLI tool that scans a Selenium Java project and detects bad practices — generates HTML and JSON reports.

---

## What it detects

| Rule | Severity | Description |
|---|---|---|
| `THREAD_SLEEP` | 🔴 Critical | `Thread.sleep()` — causes flaky tests |
| `DIRECT_DRIVER` | 🔴 Critical | `new ChromeDriver()` in tests — blocks parallelism |
| `FRAGILE_XPATH` | 🟡 Warning | Positional XPath `//div[3]` — breaks on UI changes |
| `MISSING_POM` | 🟡 Warning | `findElement()` in test class — violates Page Object Model |
| `MISSING_ASSERTION` | 🟡 Warning | Test method with no assertion — always passes |

---

## Usage

```bash
# Build
mvn package

# Audit a project (generates HTML + JSON reports)
java -jar target/selenium-audit-toolkit-1.0.0.jar /path/to/your/selenium/project

# HTML report only
java -jar target/selenium-audit-toolkit-1.0.0.jar /path/to/project --format html

# Fail the build if critical issues found (CI mode)
java -jar target/selenium-audit-toolkit-1.0.0.jar /path/to/project --fail-on-critical
```

### Output example

```
🔍 Selenium Audit Toolkit v1.0.0
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Project : /my-selenium-project

Files scanned : 24
Issues found  : 8 (🔴 3 critical, 🟡 5 warning, 🔵 0 info)

Top issues:
  [CRITICAL] Thread.sleep() found — hard-coded wait (line 42)
  [CRITICAL] new ChromeDriver() — direct driver instantiation (line 15)
  [WARNING]  findElement() in test class — violates POM (line 67)
  ...

Score : 55/100 — Needs improvement
📄 HTML report : audit-report.html
📋 JSON report : audit-report.json
```

---

## Integrate in CI (GitHub Actions)

```yaml
- name: Audit Selenium project
  run: |
    java -jar selenium-audit-toolkit.jar ./src --fail-on-critical --format json
    
- name: Upload audit report
  uses: actions/upload-artifact@v4
  with:
    name: selenium-audit
    path: audit-report.html
```

---

## Stack

| Tool | Role |
|---|---|
| JavaParser | AST analysis of Java files |
| Picocli | CLI interface |
| Jackson | JSON report generation |
| Maven Shade | Executable fat JAR |

---

## Author

**Fatima El Madini** — Senior Test Automation Engineer  
[LinkedIn](https://linkedin.com/in/yourprofile) · [Malt](https://malt.fr/profile/yourprofile)  
Portfolio : [Projet 1](https://github.com/MadiFati/selenium-e2e-framework) · [Projet 2](https://github.com/MadiFati/api-ui-testing-combo)
