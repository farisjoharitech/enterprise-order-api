# Day 8 — Exception Handling & Error Architecture

## Objective

Create a clean exception architecture for the enterprise-order-api.

## Exception Flow

```text
Business Problem
      ↓
Domain Exception
      ↓
Global Exception Handler
      ↓
HTTP Response
```

Example:

```text
Order doesn't exist
      ↓
OrderNotFoundException
      ↓
@RestControllerAdvice
      ↓
404 Not Found
```

## Exception Types

### Validation Error

Invalid client input.

Example:

```text
quantity = 0
customerId = null
```

Response:

```text
400 Bad Request
```

### Domain Error

Business condition.

Example:

```text
Order ID does not exist
```

Response:

```text
404 Not Found
```

### Infrastructure Error

Example:

```text
Database unavailable
```

Typically:

```text
500 Internal Server Error
```

## Domain Exception

Use meaningful application exceptions.

Example:

```java
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }
}
```

## Global Exception Handling

Use:

```java
@RestControllerAdvice
```

to centralize REST exception handling.

Example flow:

```text
Controller
    ↓
Service
    ↓
OrderNotFoundException
    ↓
GlobalExceptionHandler
    ↓
HTTP 404
```

## Controller Responsibility

Controllers should remain thin.

Avoid putting business exception handling directly inside every controller.

Bad:

```java
try {
    service.getOrder(id);
} catch (...) {
    ...
}
```

Prefer:

```text
Controller
    ↓
Service
    ↓
Exception
    ↓
Global Handler
```

## Error Contract

REST APIs should return predictable error structures.

Existing validation contract:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "fieldErrors": []
}
```

Preserve the existing API contract.

## Transaction Relationship

```text
@Transactional
      ↓
BEGIN
      ↓
Business Operation
      ↓
RuntimeException
      ↓
ROLLBACK
```

Exception handling and transactions are closely related.

## Important Rules

* Do not use broad `catch (Exception)` unnecessarily.
* Do not expose stack traces to API clients.
* Do not put business logic in controllers.
* Do not couple services unnecessarily to HTTP.
* Do not log secrets or sensitive information.
* Preserve existing API error contracts.
* Use meaningful domain exceptions.

## HTTP Mapping

```text
Validation       → 400
Not Found        → 404
Business Conflict→ 409
Infrastructure   → 500
Unexpected Error → 500
```

Mappings depend on actual business semantics.

## Tests

* Existing order → 200
* Missing order → 404
* Invalid request → 400
* Successful POST → success
* Missing order on PUT → 404
* Missing order on DELETE → 404

## Architecture

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Domain Exception
  ↓
Global Exception Handler
  ↓
HTTP Response
```

## Key Lessons

1. Exceptions should represent meaningful failures.
2. Services should express business problems, not HTTP mechanics.
3. Global exception handling keeps controllers thin.
4. Validation errors and business errors are different.
5. Runtime exceptions interact naturally with Spring transaction rollback.
6. Stable error contracts make APIs easier for clients to consume.
7. Never hide every error behind a generic 500 response.
8. Never expose sensitive information in logs or error responses.

## Day 8 Definition of Done

* [ ] Domain exception implemented if required
* [ ] Global exception handler updated
* [ ] Existing validation preserved
* [ ] 404 behavior verified
* [ ] 400 behavior verified
* [ ] REST API tested
* [ ] Automated tests pass
* [ ] Clean tests pass
* [ ] Git diff reviewed
* [ ] Documentation updated
* [ ] Git commit created
* [ ] GitHub push completed
