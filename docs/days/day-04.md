# Day 4 — DTOs, Validation & API Boundaries

**Project:** `enterprise-order-api`
**Track:** Java Enterprise Backend / Banking Backend Mastery
**Day:** 4 / 365
**Focus:** DTOs, Bean Validation, API boundaries, request/response models, entity protection, mapping, and validation testing

---

## 1. Learning Objective

Day 4 establishes a clean boundary between the external REST API and the internal application/database model.

The target architecture is:

```text
Client
  │
  │ JSON
  ▼
Request DTO
  │
  │ @Valid
  ▼
Controller
  │
  ▼
Service
  │
  ▼
Entity
  │
  ▼
Repository
  │
  ▼
PostgreSQL
```

Response:

```text
PostgreSQL
  │
  ▼
Repository
  │
  ▼
Entity
  │
  ▼
Service
  │
  ▼
Response DTO
  │
  ▼
JSON
  │
  ▼
Client
```

The key rule is:

> **Entity ≠ API contract.**

---

# 2. What Is a DTO?

DTO means:

> **Data Transfer Object**

A DTO is an object used to transfer data across an application boundary.

Example:

```java
public record CreateOrderRequest(
        Long customerId,
        String productName,
        Integer quantity,
        String status
) {
}
```

A response DTO might be:

```java
public record OrderResponse(
        Long id,
        Long customerId,
        String productName,
        Integer quantity,
        String status
) {
}
```

The DTO contains only information required by the API.

---

# 3. Why Not Expose JPA Entities?

Suppose the entity contains:

```text
Order
├── id
├── customer
├── customerName
├── productName
├── quantity
├── status
├── createdAt
└── version
```

The API may only need:

```text
OrderResponse
├── id
├── customerId
├── productName
├── quantity
└── status
```

If we expose the entity directly, internal implementation details become part of the API.

That creates unnecessary coupling.

---

# 4. DTO Architecture

Use:

```text
External API
     │
     ▼
    DTO
     │
     ▼
Application
     │
     ▼
Domain / Entity
     │
     ▼
Database
```

Instead of:

```text
External API
     │
     ▼
JPA Entity
     │
     ▼
Database
```

The first design gives us much greater control over API evolution.

---

# 5. Request DTO

A request DTO represents data the client is allowed to submit.

Example:

```java
public record CreateOrderRequest(

        Long customerId,

        String productName,

        Integer quantity,

        String status

) {
}
```

Notice what is missing:

```text
id
createdAt
version
```

Those fields should normally be controlled by the server.

---

# 6. Response DTO

A response DTO represents data the server chooses to expose.

Example:

```java
public record OrderResponse(
        Long id,
        Long customerId,
        String productName,
        Integer quantity,
        String status
) {
}
```

The response contract does not have to match the database table.

---

# 7. Create vs Update DTO

Do not automatically use one DTO for every operation.

Creation might require:

```text
customerId
productName
quantity
status
```

An update might allow only:

```text
quantity
status
```

Therefore:

```text
CreateOrderRequest
UpdateOrderRequest
OrderResponse
```

may be three different models.

This gives each operation an explicit contract.

---

# 8. Bean Validation

Spring Boot supports Jakarta Bean Validation.

Common annotations include:

```text
@NotNull
@NotBlank
@Size
@Min
@Max
@Positive
@PositiveOrZero
@Email
@Pattern
```

Example:

```java
public record CreateOrderRequest(

        @NotNull
        Long customerId,

        @NotBlank
        @Size(max = 100)
        String productName,

        @NotNull
        @Positive
        Integer quantity,

        @NotBlank
        String status

) {
}
```

---

# 9. `@NotNull`

Checks that the value is not `null`.

Example:

```java
@NotNull
Long customerId
```

Invalid:

```json
{
  "customerId": null
}
```

---

# 10. `@NotBlank`

Use for required text.

```java
@NotBlank
String productName
```

It rejects:

```text
null
""
"   "
```

---

# 11. `@Size`

Controls the size of strings or collections.

Example:

```java
@Size(max = 100)
String productName
```

This prevents unnecessarily large input.

---

# 12. `@Positive`

Useful for quantities.

```java
@Positive
Integer quantity
```

Valid:

```text
1
2
10
100
```

Invalid:

```text
0
-1
-100
```

---

# 13. `@Valid`

The controller can trigger validation using:

```java
@Valid
```

Example:

```java
@PostMapping
public ResponseEntity<OrderResponse> create(
        @Valid @RequestBody CreateOrderRequest request) {

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(orderService.createOrder(request));
}
```

The flow becomes:

```text
HTTP Request
     ↓
JSON
     ↓
DTO
     ↓
@Valid
     ↓
Validation
     ↓
Controller
     ↓
Service
```

---

# 14. Validation vs Business Rules

This distinction is critical.

### Structural validation

```text
quantity > 0
productName is not blank
customerId is not null
```

These are suitable for Bean Validation.

### Business rules

```text
Customer cannot place an order
when account is suspended.

Customer cannot exceed
daily transaction limit.

Order cannot be cancelled
after settlement.
```

These belong in application/domain logic.

Therefore:

```text
Bean Validation
      ↓
Input constraints

Service / Domain
      ↓
Business rules
```

Do not put complex business rules into DTO annotations.

---

# 15. Validation vs Database Constraints

Validation and database constraints protect different layers.

```text
API
 ↓
@NotNull
@NotBlank
@Positive
 ↓
Application
 ↓
Business rules
 ↓
Database
 ↓
NOT NULL
UNIQUE
FOREIGN KEY
CHECK
```

For example:

```text
API:
customerId must be supplied.

Application:
customer must be allowed to place order.

Database:
customer_id must reference a valid customer.
```

Defense in depth is important.

---

# 16. Fail Fast

Invalid input should be rejected as early as possible.

Bad:

```text
Request
 ↓
Service
 ↓
Repository
 ↓
Database
 ↓
Database rejects invalid data
```

Better:

```text
Request
 ↓
DTO Validation
 ↓
400 Bad Request
```

This is the **Fail Fast** principle.

---

# 17. Mapping

Mapping converts between API models and internal models.

Request:

```text
CreateOrderRequest
        ↓
      Order
```

Response:

```text
Order
   ↓
OrderResponse
```

Example:

```java
Order order = new Order();

order.setProductName(request.productName());
order.setQuantity(request.quantity());
order.setStatus(request.status());
```

---

# 18. Entity → DTO Mapping

Example:

```java
private OrderResponse toResponse(Order order) {

    return new OrderResponse(
            order.getId(),
            order.getCustomer().getId(),
            order.getProductName(),
            order.getQuantity(),
            order.getStatus()
    );
}
```

Mapping makes the public API deliberate.

---

# 19. Why Mapping Matters

Without mapping:

```text
Entity changes
     ↓
API changes
```

With DTOs:

```text
Entity changes
     ↓
Mapping layer
     ↓
API can remain stable
```

This is especially important for enterprise systems.

---

# 20. API Contract

The API contract defines what consumers can expect.

For example:

```http
POST /api/v1/orders
```

Request:

```json
{
  "customerId": 10,
  "productName": "Laptop",
  "quantity": 2,
  "status": "PENDING"
}
```

Response:

```json
{
  "id": 101,
  "customerId": 10,
  "productName": "Laptop",
  "quantity": 2,
  "status": "PENDING"
}
```

The API contract should not expose internal database implementation unnecessarily.

---

# 21. Backward Compatibility

Suppose today's response is:

```json
{
  "id": 101,
  "productName": "Laptop",
  "quantity": 2
}
```

Later we add:

```json
{
  "id": 101,
  "productName": "Laptop",
  "quantity": 2,
  "createdAt": "2026-09-08T10:00:00Z"
}
```

This may be backward-compatible for many clients.

But changing:

```text
productName
```

to:

```text
product
```

can break consumers.

DTOs give us control over these changes.

---

# 22. Security Boundary

DTOs are also a security mechanism.

Suppose the entity contains:

```text
id
version
createdAt
internalStatus
```

The client should not automatically be allowed to submit these.

A request DTO can exclude them entirely.

Therefore:

```text
DTO design
    +
validation
    +
authorization
```

forms part of the API security boundary.

---

# 23. Mass Assignment Problem

A dangerous design is:

```text
JSON
 ↓
Entity
 ↓
save()
```

where the client can potentially influence fields that should be server-controlled.

Example:

```json
{
  "id": 999,
  "version": 999,
  "createdAt": "2020-01-01T00:00:00Z"
}
```

A properly designed request DTO does not contain those fields.

This is one reason DTOs are important in enterprise applications.

---

# 24. Day 14 Connection — Optimistic Locking

Day 14 introduced:

```java
@Version
private Long version;
```

The version is managed by JPA/Hibernate.

The client should not manually control it through a normal create/update DTO.

Remember:

```text
version
    ↓
server-controlled
    ↓
Hibernate-managed
```

Do not manually increment it.

---

# 25. DTOs and DDD

DTO:

```text
Transport / API model
```

Entity/domain object:

```text
Business/domain model
```

They are not automatically the same thing.

Later, the project may evolve toward:

```text
REST DTO
   ↓
Application Layer
   ↓
Domain Model
   ↓
Repository
```

This separation becomes increasingly valuable as the banking domain becomes more complex.

---

# 26. DTOs and Contract-First Development

The project will later introduce OpenAPI.

The relationship will be:

```text
OpenAPI Contract
       ↓
Request/Response Models
       ↓
DTOs
       ↓
Controller
       ↓
Service
```

The database should not dictate the public API.

---

# 27. DTOs and TDD

Validation should be tested.

Example requirement:

> Quantity must be greater than zero.

TDD:

```text
RED
 ↓
Write failing test
 ↓
GREEN
 ↓
Add @Positive
 ↓
REFACTOR
```

The test should verify API behavior rather than simply checking that an annotation exists.

---

# 28. Testing Pyramid

For this feature:

```text
              E2E
             /   \
        Integration
           /     \
       Web Tests
          /       \
      Unit Tests
```

Different tests answer different questions.

### Unit tests

Test service behavior.

### Web tests

Test:

```text
HTTP
DTO
Validation
Controller
```

### Integration tests

Test:

```text
Application
Database
JPA
Transactions
```

---

# 29. Validation Test Cases

At minimum:

```text
Valid request
Missing customerId
Blank productName
Null quantity
Zero quantity
Negative quantity
Oversized productName
Missing status
```

Expected invalid requests:

```http
400 Bad Request
```

---

# 30. Existing Project Inspection

Before modifying the project, inspect the existing implementation.

This is important because Days 1–3 have already established functionality.

Navigate to:

```text
src/main/java/com/faris/enterprise_order_api
```

Inspect:

```text
controller
dto
service
repository
model
```

Do not replace working code merely to match an example.

The actual repository is the source of truth.

---

# 31. Windows — Navigate to Project

Open PowerShell.

Run:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Verify:

```powershell
Get-Location
```

Expected:

```text
C:\Users\User\Documents\JavaProjects\enterprise-order-api
```

---

# 32. Check Git State

Run:

```powershell
git status
```

Then:

```powershell
git log --oneline -8
```

Make sure the previous day's work is committed before starting Day 4.

---

# 33. Inspect Java Structure

Run:

```powershell
Get-ChildItem .\src\main\java\com\faris\enterprise_order_api -Recurse
```

Inspect DTO files:

```powershell
Get-ChildItem .\src\main\java\com\faris\enterprise_order_api\dto
```

If the DTO package has a different structure, use the actual repository structure rather than creating unnecessary duplicate packages.

---

# 34. IntelliJ — Inspect DTOs

Open IntelliJ IDEA.

Click:

```text
File
  ↓
Open
```

Select:

```text
C:\Users\User\Documents\JavaProjects\enterprise-order-api
```

Click:

```text
OK
```

Then open:

```text
src
 └── main
      └── java
           └── com.faris.enterprise_order_api
```

Inspect:

```text
controller
dto
service
repository
model
```

---

# 35. Verify Validation Dependency

Open:

```text
pom.xml
```

Search:

```text
validation
```

The project already includes Spring validation support.

Do not add duplicate dependencies.

Principles:

```text
DRY
KISS
YAGNI
```

---

# 36. Implement Request DTO Validation

If the existing implementation needs a request DTO, use an appropriate structure such as:

```java
public record CreateOrderRequest(

        @NotNull
        Long customerId,

        @NotBlank
        @Size(max = 100)
        String productName,

        @NotNull
        @Positive
        Integer quantity,

        @NotBlank
        String status

) {
}
```

Adapt this to the existing project contract.

Do not rename existing API fields without a reason.

---

# 37. Controller Validation

The controller should use:

```java
@Valid
@RequestBody
```

Example:

```java
@PostMapping
public ResponseEntity<?> create(
        @Valid @RequestBody CreateOrderRequest request) {

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(orderService.createOrder(request));
}
```

The service should receive validated input.

---

# 38. Service Responsibility

The service should:

```text
Receive DTO
     ↓
Resolve related entities
     ↓
Apply business rules
     ↓
Create/update entity
     ↓
Persist
     ↓
Map result to response DTO
```

The controller should not perform this entire workflow.

---

# 39. Customer Resolution

The request contains:

```text
customerId
```

The service should resolve the customer using the repository.

Conceptually:

```java
Customer customer =
        customerRepository.findById(request.customerId())
                .orElseThrow(...);
```

The client should not submit an entire arbitrary `Customer` entity.

This is both cleaner and safer.

---

# 40. Response Mapping

Map the entity to the response DTO.

Conceptually:

```java
private OrderResponse toResponse(Order order) {

    return new OrderResponse(
            order.getId(),
            order.getCustomer().getId(),
            order.getProductName(),
            order.getQuantity(),
            order.getStatus()
    );
}
```

Use the actual entity/API fields present in the project.

---

# 41. Validation Error Response

The project already has centralized exception handling.

Do not create a second unrelated error format.

The target should be one consistent structure, for example:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "quantity",
      "message": "must be greater than 0"
    }
  ]
}
```

The exact format should follow the existing project's error contract.

---

# 42. Manual API Test — Invalid Request

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

Open another PowerShell window.

Navigate:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Create invalid JSON:

```powershell
$body = @{
    customerId = 1
    productName = ""
    quantity = 0
    status = "PENDING"
} | ConvertTo-Json
```

Send:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/orders" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

Expected behavior:

```text
400 Bad Request
```

---

# 43. Manual API Test — Valid Request

Example:

```powershell
$body = @{
    customerId = 1
    productName = "Laptop"
    quantity = 2
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

Use a customer ID that actually exists in the local database.

Do not assume ID `1` exists.

---

# 44. Run Automated Tests

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
```

The exact test count may change as the project evolves.

Important:

```text
Failures = 0
Errors = 0
```

---

# 45. Compile Check

Run:

```powershell
.\mvnw.cmd -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

# 46. Git Review

Check:

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

Review carefully for:

```text
DTO changes
Controller changes
Service changes
Validation changes
Tests
API contract changes
```

---

# 47. Stage Changes

Run:

```powershell
git add .
```

Review:

```powershell
git diff --cached --stat
```

Then:

```powershell
git diff --cached
```

Never skip the staged diff review for an enterprise project.

---

# 48. Commit Day 4

Use:

```powershell
git commit -m "feat(api): strengthen DTO and validation boundaries"
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

# 49. Day 4 Definition of Done

```text
[ ] Understand DTO
[ ] Understand request DTO
[ ] Understand response DTO
[ ] Understand entity/API separation
[ ] Understand Bean Validation
[ ] Understand @Valid
[ ] Understand @NotNull
[ ] Understand @NotBlank
[ ] Understand @Size
[ ] Understand @Positive
[ ] Understand validation vs business rules
[ ] Understand mapping
[ ] Understand API boundaries
[ ] Understand server-controlled fields
[ ] Understand mass-assignment risk
[ ] Understand validation vs database constraints
[ ] Understand backward compatibility
[ ] Understand DTO security benefits
[ ] Inspect existing project implementation
[ ] Implement/strengthen DTO boundary
[ ] Implement/strengthen validation
[ ] Test invalid input
[ ] Test valid input
[ ] Full Maven tests pass
[ ] Save docs/days/day-04.md
[ ] Git commit
[ ] Git push
[ ] Working tree clean
```

---

# 50. Engineering Principles Applied

## SOLID

Especially:

```text
S — Single Responsibility
D — Dependency Inversion
```

DTOs should represent data transfer rather than business logic.

---

## DRY

Do not duplicate validation or error-handling logic unnecessarily.

---

## KISS

Use simple DTOs and straightforward mapping.

---

## YAGNI

Do not introduce MapStruct or complex mapper frameworks unless the project actually needs them.

---

## Separation of Concerns

```text
DTO
   ↓
Transport

Controller
   ↓
HTTP

Service
   ↓
Application/business logic

Repository
   ↓
Persistence
```

---

## High Cohesion

A DTO should have one clear purpose.

---

## Low Coupling

The API should not be tightly coupled to JPA entity structure.

---

## Encapsulation

Expose only the data necessary to external consumers.

---

## Composition

Use objects and dependencies rather than unnecessary inheritance.

---

## Fail Fast

Reject invalid input at the API boundary.

---

## Least Privilege

Clients should only be able to submit/control fields they are authorized to control.

---

## Design for Failure

Invalid input should not unnecessarily reach expensive downstream operations.

---

## Idempotency

DTOs alone do not make operations idempotent.

Idempotency must be deliberately designed.

---

## Observability

Validation failures should eventually be measurable and observable without logging sensitive data.

---

## Measure First

Do not introduce abstraction or mapping frameworks without a demonstrated need.

---

# 51. TDD / DDD / Contract-First / Security-Driven / Reliability-Driven / DevOps-Driven

These remain continuous project practices.

### TDD

```text
Requirement
   ↓
Failing test
   ↓
Implementation
   ↓
Passing test
   ↓
Refactor
```

### DDD

Keep transport DTOs separate from domain concepts.

### Contract-First

Define and protect the API contract before allowing internal implementation details to leak outward.

### Security-Driven

Treat request DTOs as part of the security boundary.

### Reliability-Driven

Reject invalid requests early.

### DevOps-Driven

Every change must be:

```text
Implemented
   ↓
Tested
   ↓
Documented
   ↓
Committed
   ↓
Pushed
```

---

# 52. Notebook Quick Reference

## DTO

```text
DTO = Data Transfer Object
```

Used for data crossing boundaries.

---

## Request

```text
JSON
 ↓
Request DTO
 ↓
Validation
 ↓
Controller
```

---

## Response

```text
Entity
 ↓
Response DTO
 ↓
JSON
```

---

## Common validation annotations

```text
@NotNull
@NotBlank
@Size
@Positive
@PositiveOrZero
@Email
@Pattern
```

---

## Critical distinction

```text
Validation
    ↓
Input constraints

Business Logic
    ↓
Business rules

Database
    ↓
Data integrity
```

---

## Architecture

```text
Client
  ↓
DTO
  ↓
Controller
  ↓
Service
  ↓
Entity
  ↓
Repository
  ↓
Database
```

---

## Security rule

> **Never allow the external request model to automatically control internal/server-owned fields.**

---

## Key rule

> **Entity ≠ API Contract**

---

## Git

```powershell
git add .
git commit -m "feat(api): strengthen DTO and validation boundaries"
git push origin main
```

---

# 53. Day 4 Final Mental Model

Remember:

```text
                 EXTERNAL WORLD
                       │
                       ▼
                 JSON Request
                       │
                       ▼
                ┌─────────────┐
                │ Request DTO │
                └──────┬──────┘
                       │
                       ▼
                    @Valid
                       │
                       ▼
                ┌─────────────┐
                │ Controller  │
                └──────┬──────┘
                       │
                       ▼
                ┌─────────────┐
                │   Service   │
                └──────┬──────┘
                       │
                       ▼
                ┌─────────────┐
                │   Entity    │
                └──────┬──────┘
                       │
                       ▼
                ┌─────────────┐
                │ Repository  │
                └──────┬──────┘
                       │
                       ▼
                  PostgreSQL
```

The response travels back through a controlled response DTO.

This boundary is one of the foundations for the future banking system.

As the project grows, the same principle will protect:

```text
Customer
Account
Payment
Transfer
Transaction
Ledger
Authentication
Authorization
Audit
Messaging
Microservices
```

from becoming tightly coupled to the database implementation.

---

# 54. Final Takeaway

A professional enterprise backend does not simply accept JSON and save it directly into a database.

Instead:

```text
External Input
      ↓
DTO
      ↓
Validation
      ↓
Controller
      ↓
Application Logic
      ↓
Domain / Entity
      ↓
Persistence
```

And:

```text
Persistence
      ↓
Entity
      ↓
Mapping
      ↓
Response DTO
      ↓
External API
```

This provides:

* API stability
* safer input handling
* cleaner architecture
* better testability
* reduced coupling
* controlled data exposure
* stronger security boundaries
* easier future evolution

**Day 4 establishes the API boundary that the rest of the enterprise application will build upon.**
