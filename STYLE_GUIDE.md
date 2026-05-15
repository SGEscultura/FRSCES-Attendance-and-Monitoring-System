# FRSCES Style Guide

## 2.1 — Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Variables | camelCase | studentId |
| Functions / Methods | camelCase | getStudentById |
| Classes | PascalCase | LoginController |
| Files | PascalCase | DatabaseConnection.java |
| Constants | UPPER_SNAKE_CASE | MAX_RETRY_COUNT |
| Database tables / fields | snake_case | student_id |

## 2.2 — Formatting Rules

| Rule | Team Decision |
|---|---|
| Indentation | 4 spaces |
| Line length limit | Max 100 characters |
| Brace style | New line for opening brace |
| Spaces vs. tabs | Spaces |
| Blank lines between functions | 1 blank line |
| Max function length | 50 lines |

## 2.3 — Commenting Standards

| Commenting Rule | Team Standard |
|---|---|
| File/module header comment | Include at top of every file |
| Function/method doc comment | Use Javadoc for public methods |
| Inline comments | Only for non-obvious logic |
| TODO comment format | // TODO: description |
| Language for comments | English |

## 2.4 — Branch Naming Strategy

| Branch Type | Naming Format | Example |
|---|---|---|
| Feature branch | feature/<short-desc> | feature/qr-scanning |
| Bug fix branch | fix/<short-desc> | fix/login-error |
| Hotfix branch | hotfix/<short-desc> | hotfix/db-connection |
| Release branch | release/<version> | release/1.0.0 |
