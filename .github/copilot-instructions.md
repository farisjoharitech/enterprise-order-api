# GitHub Copilot Project Instructions

## Project

enterprise-order-api

## Stack

- Java 17
- Spring Boot 4.1.1
- Maven
- PostgreSQL
- Spring Data JPA
- Hibernate
- JUnit

## Architecture

Controller
    ↓
Service
    ↓
Repository
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
PostgreSQL

## Rules

- Preserve the existing architecture.
- Use constructor injection.
- Keep controllers thin.
- Keep business logic in services.
- Keep persistence logic in repositories.
- Keep DTOs separate from JPA entities.
- Never expose JPA entities directly from REST controllers.
- Preserve existing validation.
- Preserve global exception handling.
- Preserve existing REST API behavior unless the task requires a change.
- Use Java 17.
- Do not upgrade dependencies unless explicitly requested.
- Do not refactor unrelated code.
- Do not introduce unnecessary abstractions.
- Do not delete tests to make them pass.
- Add tests for changed behavior.
- Do not hard-code credentials or secrets.
- Do not commit secrets.
- Do not modify unrelated files.
- Prefer the smallest correct change.

## Database

- Preserve existing naming conventions.
- Use appropriate primary keys and foreign keys.
- Use database constraints where appropriate.
- Avoid unnecessary indexes.
- Avoid N+1 query patterns.
- Prefer LAZY relationships unless there is a concrete reason otherwise.

## Testing

Use the cheapest useful validation first:

1. Targeted unit test
2. Targeted integration test
3. Full test suite when appropriate

Do not repeatedly run the complete test suite after every small change.

## Git

Do not commit automatically.

Before finishing:
- inspect git status
- inspect git diff
- verify no unrelated files changed
- report validation performed