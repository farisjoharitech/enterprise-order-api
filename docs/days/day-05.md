# Day 5 — Exception Handling & Enterprise Error Responses

**Project:** `enterprise-order-api`
**Track:** Java Enterprise Backend / Banking Backend Mastery
**Day:** 5 / 365
**Focus:** Exceptions, exception hierarchy, centralized exception handling, `@RestControllerAdvice`, `@ExceptionHandler`, HTTP error responses, error codes, validation errors, logging, security, and reliable API failure behavior

---

# 1. Day 5 Objective

A production backend must handle failures deliberately.

Today we establish a consistent error-handling architecture.

The target flow is:

```text
Client
   ↓
HTTP Request
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Exception
   ↓
Centralized Exception Handler
   ↓
Standard Error Response
   ↓
Client
```

Instead of allowing every controller to invent its own error response.

The core principle:

> **Failures are part of the API contract.**

A professional API defines not only successful responses, but also predictable failure responses.

---

# 2. Why Exception Handling Matters

Consider a request:

```http
GET /api/v1/orders/999999
```

If the order does not exist, the API should not return:

```text
NullPointerException
```

or:

```text
500 Internal Server Error
```

when the real problem is that the requested resource does not exist.

The API should return something such as:

```http
404 Not Found
```

with a structured error body.

---

# 3. Bad Exception Handling

A common beginner implementation is:

```java
try {
    ...
} catch (Exception e) {
    return ResponseEntity
            .status(500)
            .body("Something went wrong");
}
```

Problems:

* catches everything
* hides the real problem
* makes debugging difficult
* loses error classification
* creates inconsistent behavior
* may hide programming defects

Do not use one giant catch-all as the primary error-handling architecture.

---

# 4. Better Architecture

Use specific exceptions.

```text
OrderNotFoundException
CustomerNotFoundException
DuplicateCustomerException
BusinessRuleViolationException
ConflictException
```

Then map them centrally.

```text
Exception
   ↓
@RestControllerAdvice
   ↓
@ExceptionHandler
   ↓
HTTP status + error body
```

---

# 5. Exception Hierarchy

Java exceptions broadly follow:

```text
Throwable
├── Error
└── Exception
    ├── RuntimeException
    └── Checked Exceptions
```

For application/business failures, runtime exceptions are often appropriate when callers cannot reasonably recover at the point of invocation.

Example:

```java
public class OrderNotFoundException
        extends RuntimeException {
}
```

---

# 6. Checked vs Unchecked Exceptions

## Checked Exception

```java
public void process()
        throws IOException {
}
```

The compiler requires handling/declaration.

## Unchecked Exception

```java
public class OrderNotFoundException
        extends RuntimeException {
}
```

The compiler does not force explicit handling.

Spring applications commonly use unchecked exceptions for business/application failures.

The important point is not:

> Checked is bad, unchecked is good.

The important question is:

> **Does the caller have a meaningful recovery action?**

---

# 7. Domain/Application Exceptions

Example:

```java
public class OrderNotFoundException
        extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }
}
```

The service can throw:

```java
throw new OrderNotFoundException(orderId);
```

The controller does not need:

```java
try {
    ...
} catch (OrderNotFoundException e) {
    ...
}
```

The centralized handler handles it.

---

# 8. Centralized Exception Handling

Spring provides:

```java
@RestControllerAdvice
```

This allows exception handling to be centralized across controllers.

Example:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

Then:

```java
@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<ErrorResponse> handleOrderNotFound(
        OrderNotFoundException ex) {

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(...);
}
```

---

# 9. Why Centralization?

Without centralized handling:

```text
OrderController
    ├── error format A
    └── status handling

CustomerController
    ├── error format B
    └── status handling

PaymentController
    ├── error format C
    └── status handling
```

This becomes difficult to maintain.

With centralized handling:

```text
OrderController ─┐
CustomerController ├──→ GlobalExceptionHandler
PaymentController ─┘
                         ↓
                  Standard Error
```

---

# 10. Standard Error Response

A useful enterprise error model might contain:

```text
code
message
timestamp
path
details
```

Example:

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "Order not found",
  "timestamp": "2026-09-08T10:30:00Z",
  "path": "/api/v1/orders/999"
}
```

The exact structure should follow the existing project's API contract.

Do not create multiple competing error formats.

---

# 11. Error Code vs HTTP Status

These serve different purposes.

HTTP status:

```text
404
```

communicates the general protocol-level category.

Application error code:

```text
ORDER_NOT_FOUND
```

communicates application-specific meaning.

Therefore:

```text
HTTP Status
+
Application Error Code
```

is often more useful than either alone.

---

# 12. Recommended Error Codes

Examples:

```text
ORDER_NOT_FOUND
CUSTOMER_NOT_FOUND
VALIDATION_ERROR
DUPLICATE_CUSTOMER
ORDER_CONFLICT
BUSINESS_RULE_VIOLATION
INTERNAL_ERROR
```

Keep error codes stable.

Clients can use error codes for programmatic behavior.

---

# 13. HTTP Status Codes

Important codes:

| Status | Meaning                                          |
| ------ | ------------------------------------------------ |
| 200    | Successful request                               |
| 201    | Resource created                                 |
| 204    | Successful request with no body                  |
| 400    | Invalid request                                  |
| 401    | Authentication required/failed                   |
| 403    | Authenticated but not authorized                 |
| 404    | Resource not found                               |
| 409    | Resource/state conflict                          |
| 422    | Semantically invalid input, when used by the API |
| 429    | Too many requests                                |
| 500    | Unexpected server failure                        |
| 503    | Service unavailable                              |

Do not choose statuses randomly.

---

# 14. 400 vs 404

Example:

```http
GET /api/v1/orders/abc
```

If `id` must be numeric, the request itself is malformed.

Potential response:

```text
400 Bad Request
```

But:

```http
GET /api/v1/orders/999999
```

where the ID format is valid but the order does not exist:

```text
404 Not Found
```

---

# 15. 401 vs 403

These become important later when security is introduced.

### 401

The caller is not properly authenticated.

```text
Who are you?
```

### 403

The caller is authenticated but lacks permission.

```text
I know who you are,
but you cannot perform this action.
```

---

# 16. 409 Conflict

Use 409 when the request conflicts with the current resource state.

Examples:

```text
Duplicate email
Optimistic locking conflict
Invalid state transition
Duplicate business operation
```

This is particularly important because Day 14 already introduced optimistic locking.

A concurrent update conflict should not simply become a generic 500 error.

---

# 17. Day 14 Connection — Optimistic Locking

The project now contains:

```java
@Version
private Long version;
```

If two transactions attempt to update the same entity:

```text
Transaction A
       ↓
version = 0
       ↓
updates
       ↓
version = 1

Transaction B
       ↓
old version = 0
       ↓
update fails
```

The application should classify this appropriately.

A conflict can be represented as:

```http
409 Conflict
```

rather than:

```http
500 Internal Server Error
```

The exact implementation should follow the project's existing concurrency behavior.

---

# 18. Validation Errors

Day 4 introduced validation.

For example:

```java
@NotBlank
String productName;
```

If the client sends:

```json
{
  "productName": "",
  "quantity": 0
}
```

the API should produce a structured validation error.

Example:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "productName",
      "message": "must not be blank"
    },
    {
      "field": "quantity",
      "message": "must be greater than 0"
    }
  ]
}
```

---

# 19. Validation Error Structure

A good structure makes errors easy for clients to consume.

```text
ValidationError
├── code
├── message
└── details
      ├── field
      └── message
```

For example:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "email",
      "message": "must be a valid email"
    }
  ]
}
```

---

# 20. Never Leak Internal Errors

Avoid returning:

```json
{
  "exception": "org.hibernate.exception.ConstraintViolationException",
  "stackTrace": "...",
  "sql": "insert into..."
}
```

to API clients.

This exposes implementation details.

It can reveal:

* database structure
* class names
* SQL
* internal paths
* framework details
* security-sensitive information

Clients need a safe error representation.

---

# 21. Logging vs API Response

Internal logging and external responses have different purposes.

Client:

```json
{
  "code": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

Internal log:

```text
ERROR
requestId=...
exception=...
stackTrace=...
```

The server needs diagnostic information.

The client usually does not.

---

# 22. Security Principle

Never expose:

```text
password
access token
refresh token
database password
API key
private key
stack trace
SQL
internal infrastructure details
```

inside API errors.

This is:

**Least Privilege + Security-Driven Development.**

---

# 23. Correlation / Request ID

As the system becomes distributed, debugging one request across services becomes difficult.

We will eventually use something like:

```text
X-Request-ID
```

or a tracing correlation identifier.

Example:

```text
Request
  ↓
Order Service
  ↓
Payment Service
  ↓
Notification Service
```

All logs should eventually be correlated.

This will become important in the Observability section of the roadmap.

---

# 24. Error Timestamp

A timestamp helps with diagnosis.

Example:

```json
{
  "timestamp": "2026-09-08T10:30:00Z"
}
```

Prefer a consistent standard such as UTC for distributed systems.

The user's local timezone should not determine how timestamps are stored internally.

---

# 25. Error Path

Including the request path can be useful:

```json
{
  "path": "/api/v1/orders/999"
}
```

But avoid including sensitive query parameters.

Never blindly expose:

```text
/password=...
/token=...
/secret=...
```

---

# 26. Exception Handler Responsibilities

The global exception handler should:

```text
Exception
   ↓
Identify exception type
   ↓
Choose HTTP status
   ↓
Choose stable error code
   ↓
Build safe error response
   ↓
Return response
```

It should not contain business logic.

Bad:

```text
GlobalExceptionHandler
    ↓
calculate payment
    ↓
update order
    ↓
send email
```

Good:

```text
GlobalExceptionHandler
    ↓
translate exception → HTTP response
```

---

# 27. Service Responsibilities

The service should determine when a business/application failure occurs.

Example:

```java
Customer customer =
        customerRepository.findById(customerId)
            .orElseThrow(
                () -> new CustomerNotFoundException(customerId)
            );
```

The handler determines how that exception becomes HTTP.

Therefore:

```text
Service
    ↓
Business/application failure

Exception Handler
    ↓
HTTP representation
```

---

# 28. Separation of Concerns

This gives us:

```text
Controller
    ↓
HTTP request handling

Service
    ↓
Business/application logic

Repository
    ↓
Persistence

Exception
    ↓
Failure classification

Global Handler
    ↓
HTTP error translation
```

Each component has a clear responsibility.

---

# 29. Exception Translation

An important concept is translating lower-level exceptions into meaningful application exceptions.

For example:

```text
Hibernate exception
       ↓
Repository/persistence boundary
       ↓
Application exception
       ↓
HTTP error
```

The API should not expose:

```text
HibernateException
```

as its public contract.

---

# 30. Database Constraint Errors

Suppose:

```text
customers.email
    UNIQUE
```

A duplicate email may produce a database constraint exception.

The API should translate the outcome into a meaningful application response such as:

```http
409 Conflict
```

with:

```json
{
  "code": "DUPLICATE_CUSTOMER",
  "message": "A customer with this email already exists"
}
```

Do not expose the raw PostgreSQL constraint name unless there is a deliberate operational reason.

---

# 31. Error Handling and TDD

Each failure mode should have tests.

Example:

```text
Given nonexistent order
When GET /orders/{id}
Then 404
And code = ORDER_NOT_FOUND
```

Another:

```text
Given invalid quantity
When POST /orders
Then 400
And code = VALIDATION_ERROR
```

Another:

```text
Given duplicate customer email
When POST /customers
Then 409
```

TDD flow:

```text
RED
 ↓
Define expected failure contract
 ↓
GREEN
 ↓
Implement handler
 ↓
REFACTOR
```

---

# 32. Error Contract Testing

The tests should verify:

```text
HTTP status
error code
message
required fields
validation details
```

This protects the API contract.

For example:

```java
assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
```

and:

```java
assertThat(response.getBody().code())
        .isEqualTo("ORDER_NOT_FOUND");
```

---

# 33. Contract-First Connection

Errors are part of the API contract.

An API specification should eventually describe:

```text
200
201
400
401
403
404
409
500
```

and their response bodies.

Later OpenAPI will make this explicit.

---

# 34. Reliability-Driven Development

A reliable system does not pretend failures do not happen.

Instead:

```text
Failure
 ↓
Classify
 ↓
Handle
 ↓
Respond
 ↓
Log
 ↓
Measure
```

This makes failure predictable.

---

# 35. Design for Failure

Every dependency can fail:

```text
PostgreSQL
Redis
Kafka
RabbitMQ
External Payment API
Authentication Provider
```

The application must distinguish:

```text
Expected business failure
```

from:

```text
Unexpected infrastructure failure
```

Example:

```text
Order not found
    ↓
Expected
    ↓
404

Database unavailable
    ↓
Infrastructure failure
    ↓
503/appropriate server error
```

The exact status should depend on the API architecture and failure semantics.

---

# 36. Observability

Every unexpected failure should be diagnosable.

Eventually we want:

```text
Request ID
Trace ID
Timestamp
Endpoint
HTTP method
Status
Duration
Exception type
Service
```

But logs must avoid sensitive data.

---

# 37. Common Mistake — Catching Everything

Avoid:

```java
catch (Exception e) {
    return "Error";
}
```

Why?

Because it treats:

```text
OrderNotFoundException
```

the same as:

```text
NullPointerException
```

and:

```text
Database outage
```

These are completely different failures.

---

# 38. Common Mistake — Returning 500 for Everything

Bad:

```text
Customer not found → 500
Duplicate customer → 500
Invalid input → 500
Unauthorized → 500
```

A good API communicates the correct category.

---

# 39. Common Mistake — Returning Stack Traces

Never make this the production API contract.

Bad:

```json
{
  "message": "...",
  "stackTrace": [...]
}
```

Use safe error responses.

---

# 40. Common Mistake — Business Logic in Exception Handler

Avoid:

```text
ExceptionHandler
    ↓
business decision
    ↓
database operation
```

The handler should translate the already-determined failure.

---

# 41. Common Mistake — Too Many Exception Classes

Do not create:

```text
OrderNameTooLongException
OrderNameBlankException
OrderQuantityZeroException
OrderQuantityNegativeException
```

when Bean Validation can handle structural validation.

Use specific exceptions for meaningful business/application failures.

---

# 42. Common Mistake — Exception as Normal Control Flow

Do not use exceptions for ordinary branching.

Bad:

```text
try:
    find customer
catch:
    customer does not exist
```

when the repository/API provides an appropriate normal mechanism.

Exceptions should represent exceptional/failure conditions.

---

# 43. Day 5 Project Architecture

After Day 5:

```text
                         Client
                           │
                           ▼
                     REST Controller
                           │
                           ▼
                       Service
                           │
                    ┌──────┴──────┐
                    ▼             ▼
              Repository       Business
                    │             Rules
                    ▼
                Database
                    │
                    ▼
               Exception
                    │
                    ▼
          Global Exception Handler
                    │
                    ▼
             Error Response
                    │
                    ▼
                  Client
```

---

# 44. Windows — Start Day 5

Open PowerShell.

Navigate:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Verify:

```powershell
Get-Location
```

---

# 45. Check Git

Run:

```powershell
git status
```

Then:

```powershell
git log --oneline -8
```

Make sure Day 4 has already been committed.

---

# 46. Inspect Existing Exception Handling

Search the project.

PowerShell:

```powershell
Get-ChildItem .\src\main\java -Recurse -File |
    Select-String -Pattern "RestControllerAdvice|ControllerAdvice|ExceptionHandler"
```

This tells us whether centralized exception handling already exists.

This is important because the project may already contain exception handling from earlier work.

**Do not create a second competing global exception handler.**

---

# 47. Inspect Exception Classes

Run:

```powershell
Get-ChildItem .\src\main\java -Recurse -File |
    Select-String -Pattern "extends RuntimeException"
```

Look for existing exceptions such as:

```text
OrderNotFoundException
CustomerNotFoundException
```

Reuse them where appropriate.

---

# 48. Inspect Error DTOs

Run:

```powershell
Get-ChildItem .\src\main\java -Recurse -File |
    Select-String -Pattern "ErrorResponse|ApiError|errorCode|ValidationError"
```

If the project already has an error response model, extend it rather than creating another one.

---

# 49. IntelliJ — Inspect Exception Handling

Open:

```text
src
└── main
    └── java
        └── com.faris.enterprise_order_api
```

Look for packages such as:

```text
exception
error
handler
```

Inspect:

```text
@RestControllerAdvice
@ExceptionHandler
```

Understand the existing implementation before changing anything.

---

# 50. Create Exception If Needed

If the project genuinely needs a missing exception, create an appropriate class.

Example:

```java
public class OrderNotFoundException
        extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }
}
```

Do not create duplicates if one already exists.

---

# 51. Create/Improve Error Response

A simple model could be:

```java
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path
) {
}
```

For validation:

```java
public record ValidationErrorDetail(
        String field,
        String message
) {
}
```

Again, adapt this to the project's existing design.

---

# 52. Global Handler

Conceptual example:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException exception) {

        ErrorResponse response = new ErrorResponse(
                "ORDER_NOT_FOUND",
                exception.getMessage(),
                Instant.now(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}
```

The actual project implementation may use a different error model.

---

# 53. Validation Handler

The project should translate validation failures into the standard error contract.

Conceptually:

```text
MethodArgumentNotValidException
        ↓
Extract field errors
        ↓
Build validation details
        ↓
ErrorResponse
        ↓
400 Bad Request
```

Do not return raw Spring exception structures.

---

# 54. Test Not Found

Example:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/orders/999999999" `
    -Method GET
```

Expected:

```text
404 Not Found
```

with the project's structured error response.

---

# 55. Test Validation

Example:

```powershell
$body = @{
    customerId = 1
    productName = ""
    quantity = 0
    status = "PENDING"
} | ConvertTo-Json
```

Then:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/orders" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

Expected:

```text
400 Bad Request
```

and validation details.

---

# 56. Test Duplicate Resource

For a customer with a unique email constraint:

```text
POST /api/v1/customers
```

send the same email twice.

The second request should produce the application's defined conflict behavior.

Potential result:

```text
409 Conflict
```

with a stable application error code.

---

# 57. Test Existing API

Verify that successful operations still work.

Orders:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/orders" `
    -Method GET
```

Search:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/orders/search?page=0&size=20" `
    -Method GET
```

Customers:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/customers" `
    -Method GET
```

Use the exact existing endpoint paths if they differ.

---

# 58. Run Tests

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
```

Important:

```text
Failures = 0
Errors = 0
```

Do not disable failing tests simply to obtain a green build.

---

# 59. Test Exception Scenarios

At minimum verify:

```text
[ ] Order not found
[ ] Customer not found
[ ] Invalid request
[ ] Blank required field
[ ] Invalid quantity
[ ] Duplicate resource
[ ] Existing successful request still works
```

If concurrency tests are already present from Day 14:

```text
[ ] Optimistic locking conflict remains correctly handled
```

Do not duplicate the concurrency implementation from Day 14.

---

# 60. Compile

Run:

```powershell
.\mvnw.cmd -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

# 61. Review Changes

Run:

```powershell
git status
```

Then:

```powershell
git diff --stat
```

Then:

```powershell
git diff
```

Look specifically for:

```text
Exception classes
Error DTOs
Global handler
Controller changes
Service changes
Tests
Documentation
```

---

# 62. Security Review

Before committing, search for accidental sensitive information.

Do not commit:

```text
passwords
tokens
API keys
database credentials
private keys
stack traces containing secrets
```

Check:

```powershell
git diff
```

carefully.

---

# 63. Stage

Run:

```powershell
git add .
```

Then:

```powershell
git diff --cached --stat
```

Then:

```powershell
git diff --cached
```

Review the staged content.

---

# 64. Commit

Use:

```powershell
git commit -m "feat(api): standardize exception handling"
```

Push:

```powershell
git push origin main
```

Verify:

```powershell
git status
```

Expected:

```text
nothing to commit, working tree clean
```

---

# 65. Documentation

Save this file:

```text
docs/days/day-05.md
```

Verify:

```powershell
Get-Content ".\docs\days\day-05.md"
```

---

# 66. Day 5 Definition of Done

```text
[ ] Understand Java exception hierarchy
[ ] Understand checked vs unchecked exceptions
[ ] Understand application exceptions
[ ] Understand @RestControllerAdvice
[ ] Understand @ExceptionHandler
[ ] Understand centralized error handling
[ ] Understand HTTP error semantics
[ ] Understand error codes
[ ] Understand validation errors
[ ] Understand 400 vs 404
[ ] Understand 401 vs 403
[ ] Understand 409
[ ] Understand 500
[ ] Understand safe error responses
[ ] Understand exception translation
[ ] Understand logging vs client responses
[ ] Understand security implications
[ ] Understand optimistic locking conflict handling
[ ] Inspect existing project exception architecture
[ ] Avoid duplicate exception handlers
[ ] Test expected failures
[ ] Test validation failures
[ ] Test successful requests
[ ] Full Maven tests pass
[ ] Documentation saved
[ ] Git commit created
[ ] Git pushed
[ ] Working tree clean
```

---

# 67. Engineering Principles Applied

## SOLID

### Single Responsibility

The exception handler translates exceptions into HTTP responses.

It does not perform business operations.

### Dependency Inversion

Application logic should not depend directly on HTTP response construction.

---

## DRY

One centralized error-handling mechanism prevents every controller from duplicating error translation.

---

## KISS

Use a small, understandable exception hierarchy.

---

## YAGNI

Do not create dozens of exception types without meaningful distinctions.

---

## Separation of Concerns

```text
Service
    ↓
Business failure

Handler
    ↓
HTTP representation
```

---

## High Cohesion

Exception classes represent meaningful failure categories.

---

## Low Coupling

Controllers do not need to know how every exception becomes an HTTP response.

---

## Encapsulation

Internal exception details remain internal.

---

## Composition

Error responses are composed from meaningful pieces rather than relying on raw framework exceptions.

---

## Fail Fast

Invalid requests should fail before expensive processing.

---

## Least Privilege

Expose only the information the client needs to recover or understand the failure.

---

## Design for Failure

Failures are explicitly modeled rather than treated as impossible.

---

## Idempotency

Conflict handling becomes important when repeated operations can produce duplicate effects.

---

## Observability

Unexpected exceptions should be logged and traceable.

---

## Measure First

Do not create elaborate error infrastructure before understanding the application's actual failure modes.

---

# 68. TDD / DDD / Contract-First / Security-Driven / Reliability-Driven / DevOps-Driven

## TDD

Test the failure contract:

```text
Given
    missing order

When
    GET /orders/{id}

Then
    404
    ORDER_NOT_FOUND
```

---

## DDD

Exceptions should reflect meaningful business/application concepts.

Prefer:

```text
OrderNotFound
PaymentAlreadySettled
TransferLimitExceeded
```

over generic:

```text
SomethingWentWrong
```

---

## Contract-First

Errors are part of the API contract.

Document them consistently.

---

## Security-Driven

Never leak:

```text
SQL
stack traces
credentials
tokens
internal topology
```

---

## Reliability-Driven

Expected failures should produce predictable responses.

---

## DevOps-Driven

Every change must be:

```text
Implemented
 ↓
Tested
 ↓
Reviewed
 ↓
Documented
 ↓
Committed
 ↓
Pushed
```

---

# 69. Notebook Quick Reference

## Centralized Exception Handling

```text
Exception
   ↓
@RestControllerAdvice
   ↓
@ExceptionHandler
   ↓
HTTP Status
   +
Error Code
   +
Safe Message
   ↓
Client
```

---

## Important HTTP statuses

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
429 Too Many Requests
500 Internal Server Error
503 Service Unavailable
```

---

## Error response

```text
code
message
timestamp
path
details
```

---

## Key distinction

```text
Exception
    ↓
Internal implementation

Error Response
    ↓
External API contract
```

---

## Security rule

> **Never expose raw internal exceptions, stack traces, SQL, credentials, tokens, or infrastructure details to API consumers.**

---

## Main architecture

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database

Exception
    ↓
Global Handler
    ↓
Error Response
```

---

## Git

```powershell
git add .
git commit -m "feat(api): standardize exception handling"
git push origin main
```

---

# 70. Day 5 Mental Model

Remember:

```text
                  REQUEST
                     │
                     ▼
                Controller
                     │
                     ▼
                  Service
                     │
              ┌──────┴──────┐
              │             │
          Success         Failure
              │             │
              ▼             ▼
          Response      Exception
                            │
                            ▼
                 Global Exception Handler
                            │
                            ▼
                    Standard Error DTO
                            │
                            ▼
                         Client
```

The backend must be designed for both paths:

```text
SUCCESS
FAILURE
```

not just success.

---

# 71. Day 5 Final Takeaway

A mature backend does not treat exceptions as accidental implementation details.

It deliberately defines:

```text
What failed?
      ↓
Why did it fail?
      ↓
Is it expected?
      ↓
What HTTP status represents it?
      ↓
What stable error code represents it?
      ↓
What information can safely be exposed?
      ↓
What should be logged?
```

The resulting architecture is:

```text
Business/Application Exception
          ↓
Centralized Exception Handler
          ↓
Stable API Error Contract
          ↓
Client
```

This foundation becomes critical when the project later introduces:

```text
Authentication
Authorization
Transactions
Payments
Messaging
Kafka
Redis
Microservices
Distributed Transactions
Retries
Circuit Breakers
Observability
Kubernetes
CI/CD
Cloud
```

because enterprise systems are defined not only by how they handle successful requests, but by **how predictably and safely they handle failure**.
