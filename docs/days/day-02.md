Below is the complete **`docs/days/day-02.md`** reference, designed to fit the actual evolving `enterprise-order-api` project and remain useful as a standalone offline notebook/reference.

# Day 2 — REST API & HTTP Fundamentals

## Java Enterprise Backend Mastery

**Project:** `enterprise-order-api`
**Day:** 2 / 365
**Focus:** REST API, HTTP, Resources, CRUD, Status Codes, Request/Response Design
**Architecture:** Controller → Service → Repository → Database
**Java:** 17
**Framework:** Spring Boot
**Database:** PostgreSQL
**Build:** Maven
**Version Control:** Git / GitHub

---

# 1. Day 2 Objective

Day 2 focuses on understanding how clients communicate with the backend through HTTP and REST.

The goal is not simply to create endpoints.

The goal is to understand:

```text
Client
  ↓
HTTP Request
  ↓
Spring Boot
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Database
  ↓
HTTP Response
  ↓
Client
```

The Order API created today becomes the foundation for all future functionality.

---

# 2. Project Principle

This is one continuously evolving application.

```text
Day 1
Project Foundation
       ↓
Day 2
REST / HTTP
       ↓
Day 3
Domain Model
       ↓
Day 4
Validation
       ↓
...
       ↓
Day 365
Enterprise Banking Backend
```

Do not create separate REST practice projects.

---

# 3. What Is REST?

REST means:

> Representational State Transfer.

REST is an architectural style for designing networked applications.

A REST API models business objects as **resources**.

For this project:

```text
/orders
/customers
```

are resources.

---

# 4. Resource-Oriented Design

Instead of designing endpoints around verbs:

```text
/createOrder
/updateOrder
/deleteOrder
```

prefer resources:

```text
/orders
/orders/{id}
```

and use HTTP methods to express the operation.

---

# 5. HTTP + Resource

The basic model is:

```text
HTTP Method
      +
Resource
      +
Representation
```

Example:

```http
GET /api/v1/orders/10
```

means:

> Retrieve order 10.

---

# 6. REST CRUD Mapping

The standard mapping:

```text
HTTP Method    Resource        Meaning
------------------------------------------------
GET            /orders         List orders
GET            /orders/{id}    Get one order
POST           /orders         Create order
PUT            /orders/{id}    Replace/update order
DELETE         /orders/{id}    Delete order
```

This creates predictable APIs.

---

# 7. Current Order API

The project exposes:

```text
GET    /api/v1/orders
GET    /api/v1/orders/{id}
POST   /api/v1/orders
PUT    /api/v1/orders/{id}
DELETE /api/v1/orders
```

The exact supported endpoint structure may evolve as the project grows.

---

# 8. API Base Path

The API starts with:

```text
/api/v1
```

Therefore:

```text
/api/v1/orders
```

is the collection resource.

And:

```text
/api/v1/orders/1
```

is a specific order resource.

---

# 9. Why `/api/v1`?

API versioning helps manage future changes.

Current:

```text
/api/v1/orders
```

Potential future version:

```text
/api/v2/orders
```

The important idea is:

> API contracts need to evolve without unexpectedly breaking existing consumers.

Detailed API versioning will be studied later.

---

# 10. HTTP Request

An HTTP request consists conceptually of:

```text
Request Line
Headers
Body
```

Example:

```http
POST /api/v1/orders HTTP/1.1
Content-Type: application/json

{
  "productName": "Mechanical Keyboard",
  "quantity": 2,
  "customerId": 1,
  "status": "PENDING"
}
```

---

# 11. HTTP Request Line

Example:

```http
POST /api/v1/orders HTTP/1.1
```

Contains:

```text
POST
/api/v1/orders
HTTP/1.1
```

Meaning:

```text
method
path
HTTP version
```

---

# 12. HTTP Headers

Headers carry metadata.

Example:

```http
Content-Type: application/json
Accept: application/json
```

Important headers include:

```text
Content-Type
Accept
Authorization
Cache-Control
User-Agent
Host
```

Authentication and authorization headers become important later.

---

# 13. Content-Type

For JSON requests:

```http
Content-Type: application/json
```

means:

> The request body is JSON.

Example:

```json
{
  "productName": "Keyboard",
  "quantity": 2
}
```

---

# 14. Accept

The `Accept` header communicates the response formats the client can handle.

Example:

```http
Accept: application/json
```

Meaning:

> Prefer JSON as the response representation.

---

# 15. HTTP Response

An HTTP response contains:

```text
Status Code
Headers
Body
```

Example:

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "productName": "Keyboard"
}
```

---

# 16. HTTP Status Codes

Status codes communicate the result.

Important groups:

```text
1xx Informational
2xx Success
3xx Redirection
4xx Client Error
5xx Server Error
```

For backend development, especially understand:

```text
200
201
204
400
401
403
404
409
422
500
```

---

# 17. 200 OK

Use when the request succeeds and a response representation is returned.

Example:

```http
GET /api/v1/orders/1
```

Response:

```http
200 OK
```

---

# 18. 201 Created

Use when a resource has been successfully created.

Example:

```http
POST /api/v1/orders
```

Response:

```http
201 Created
```

The response can include the newly created resource.

---

# 19. 204 No Content

Use when the operation succeeds but there is no response body.

Common example:

```http
DELETE /api/v1/orders/1
```

Response:

```http
204 No Content
```

---

# 20. 400 Bad Request

Use when the request is malformed or invalid at the request boundary.

Examples:

```text
invalid JSON
invalid parameter
validation failure
invalid request structure
```

Example:

```http
400 Bad Request
```

---

# 21. 401 Unauthorized

Means:

> The client has not provided valid authentication credentials.

Later the project will introduce:

```text
JWT
OAuth2
OIDC
Authentication
```

---

# 22. 403 Forbidden

Means:

> The client is authenticated but is not allowed to perform the operation.

Example:

```text
User authenticated
      ↓
Attempts admin operation
      ↓
403 Forbidden
```

---

# 23. 404 Not Found

Use when a requested resource does not exist.

Example:

```http
GET /api/v1/orders/999999
```

If order 999999 doesn't exist:

```http
404 Not Found
```

---

# 24. 409 Conflict

Used when the request conflicts with the current state of the resource.

Examples:

```text
duplicate resource
concurrent modification
business state conflict
```

This will become particularly important later when the project handles:

```text
optimistic locking
idempotency
payments
transfers
```

---

# 25. 500 Internal Server Error

Represents an unexpected server-side failure.

Do not expose:

```text
stack trace
SQL details
internal class names
database credentials
```

to the client.

---

# 26. REST Resource Naming

Prefer nouns:

```text
/orders
/customers
/accounts
/payments
```

Avoid unnecessary verbs:

```text
/getOrders
/createOrder
/deleteOrder
```

HTTP methods already express the action.

---

# 27. Collection vs Individual Resource

Collection:

```text
/orders
```

Individual resource:

```text
/orders/123
```

Mental model:

```text
/orders
   │
   ├── order 1
   ├── order 2
   ├── order 3
   └── ...
```

---

# 28. Nested Resources

A possible future relationship:

```text
/customers/10/orders
```

means:

> Orders belonging to customer 10.

However, nested resources should be used carefully.

Do not create excessively deep paths:

```text
/customers/1/accounts/2/transactions/3/items/4
```

Complexity should be justified.

---

# 29. HTTP GET

GET retrieves information.

Example:

```http
GET /api/v1/orders
```

Should generally not modify server state.

This property is called:

> Safe.

---

# 30. HTTP POST

POST commonly creates a resource or triggers an operation that is not naturally represented as a replacement.

Example:

```http
POST /api/v1/orders
```

Request:

```json
{
  "productName": "Keyboard",
  "quantity": 2,
  "customerId": 1,
  "status": "PENDING"
}
```

---

# 31. HTTP PUT

PUT is commonly used to replace a resource representation.

Example:

```http
PUT /api/v1/orders/1
```

PUT is generally expected to be **idempotent**.

---

# 32. Idempotency

An operation is idempotent when repeating it produces the same intended resulting state.

Example:

```text
PUT order 1
status = CONFIRMED
```

Repeat:

```text
PUT order 1
status = CONFIRMED
```

The intended final state remains:

```text
CONFIRMED
```

---

# 33. POST and Idempotency

POST is not inherently idempotent.

Example:

```text
POST /orders
```

sent twice could create:

```text
Order 101
Order 102
```

This becomes extremely important for banking/payment systems.

Later the project will implement:

```text
Idempotency keys
Duplicate-request protection
Distributed idempotency
```

---

# 34. HTTP DELETE

DELETE removes a resource.

Example:

```http
DELETE /api/v1/orders/1
```

A successful deletion may return:

```http
204 No Content
```

---

# 35. Statelessness

REST systems are generally designed to be stateless.

This means each request should contain the information necessary for the server to process it rather than depending on hidden server-side conversational state.

Conceptually:

```text
Request 1
   ↓
Server

Request 2
   ↓
Server
```

The server should not require undocumented state from Request 1 to understand Request 2.

Authentication tokens can carry client authentication context while the API remains stateless from the REST interaction perspective.

---

# 36. Controller Responsibility

The Spring controller is the HTTP boundary.

Responsibilities:

```text
HTTP routing
Request parameters
Request body
Validation boundary
Response status
Response DTO
```

Not:

```text
SQL
database transaction logic
complex business rules
```

---

# 37. Example Controller Structure

Conceptually:

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @GetMapping
    public ResponseEntity<?> getOrders() {
        // ...
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody CreateOrderRequest request) {
        // ...
    }
}
```

The exact implementation should follow the current repository rather than blindly replacing existing code.

---

# 38. `@RestController`

`@RestController` identifies a Spring MVC controller whose methods return response bodies.

Conceptually:

```java
@RestController
public class OrderController {
}
```

It combines controller semantics with response-body behavior.

---

# 39. `@RequestMapping`

Defines a base URL path.

Example:

```java
@RequestMapping("/api/v1/orders")
```

Then methods can define:

```java
@GetMapping
@PostMapping
@PutMapping("/{id}")
@DeleteMapping("/{id}")
```

---

# 40. `@GetMapping`

Example:

```java
@GetMapping
public ResponseEntity<?> getOrders() {
}
```

Maps:

```http
GET /api/v1/orders
```

---

# 41. `@PostMapping`

Example:

```java
@PostMapping
public ResponseEntity<?> createOrder(...) {
}
```

Maps:

```http
POST /api/v1/orders
```

---

# 42. `@PutMapping`

Example:

```java
@PutMapping("/{id}")
```

Maps:

```http
PUT /api/v1/orders/123
```

---

# 43. `@DeleteMapping`

Example:

```java
@DeleteMapping("/{id}")
```

Maps:

```http
DELETE /api/v1/orders/123
```

---

# 44. Path Variables

Example URL:

```text
/api/v1/orders/123
```

The `123` can be captured:

```java
@GetMapping("/{id}")
public ResponseEntity<?> getOrder(
        @PathVariable Long id) {
}
```

---

# 45. Query Parameters

Example:

```text
/api/v1/orders?status=PENDING
```

Query parameters are useful for filtering and searching.

Later the project expands this into:

```text
status
customerId
page
size
sort
```

---

# 46. Path Variable vs Query Parameter

Use path variable for identifying a specific resource:

```text
/orders/123
```

Use query parameter for filtering/options:

```text
/orders?status=PENDING
```

Mental model:

```text
Path
 ↓
Which resource?

Query
 ↓
How should I query the collection?
```

---

# 47. Request Body

POST/PUT commonly carry JSON.

Example:

```json
{
  "productName": "Mechanical Keyboard",
  "quantity": 2,
  "customerId": 1,
  "status": "PENDING"
}
```

Spring can deserialize JSON into a Java DTO.

---

# 48. JSON → Java

Conceptually:

```text
JSON
 ↓
Jackson
 ↓
Java DTO
```

Example:

```text
{
  "quantity": 2
}
```

becomes:

```java
request.getQuantity()
```

---

# 49. Java → JSON

Response:

```java
OrderResponse
```

is serialized into:

```json
{
  "id": 1,
  "productName": "Mechanical Keyboard"
}
```

Conceptually:

```text
Java object
 ↓
Jackson
 ↓
JSON
```

---

# 50. Why DTO Instead of Entity?

Do not make the database entity the public API contract by default.

Prefer:

```text
HTTP
 ↓
Request DTO
 ↓
Service
 ↓
Entity
 ↓
Database
```

and:

```text
Database
 ↓
Entity
 ↓
Response DTO
 ↓
HTTP
```

Benefits:

```text
encapsulation
API stability
security
controlled fields
```

---

# 51. API Boundary

The controller is a boundary.

Think:

```text
              SYSTEM
────────────────────────────────
              │
HTTP          │
Client ───────┤ Controller
              │
              ▼
           Service
              ▼
          Repository
              ▼
           Database
────────────────────────────────
```

The controller protects the internal architecture from external representation concerns.

---

# 52. Separation of Concerns

A good controller:

```text
receive
validate
delegate
respond
```

Not:

```text
receive
validate
calculate
query database
send email
publish Kafka event
update cache
handle SQL
```

Keep the controller thin.

---

# 53. Example Request Flow

Create order:

```text
POST /api/v1/orders
        │
        ▼
OrderController
        │
        ▼
CreateOrderRequest
        │
        ▼
OrderService
        │
        ▼
OrderRepository
        │
        ▼
PostgreSQL
        │
        ▼
OrderResponse
        │
        ▼
201 Created
```

---

# 54. GET Request Flow

```text
GET /api/v1/orders/10
        │
        ▼
Controller
        │
        ▼
Service
        │
        ▼
Repository
        │
        ▼
PostgreSQL
        │
        ▼
OrderResponse
        │
        ▼
200 OK
```

---

# 55. Error Flow

Example:

```text
GET /api/v1/orders/999999
        │
        ▼
Controller
        │
        ▼
Service
        │
        ▼
Repository
        │
        ▼
Not Found
        │
        ▼
Exception Handler
        │
        ▼
404 Not Found
```

Centralized exception handling keeps this behavior consistent.

---

# 56. REST and Database Are Different Layers

Do not confuse:

```text
HTTP resource
```

with:

```text
database table
```

They often correspond conceptually but do not have to be identical.

Example:

```text
API:
OrderResponse

Database:
orders + customers
```

One API resource can come from multiple database tables.

---

# 57. REST API Design Rule

Design the API around the consumer's business needs.

Not:

```text
database table → automatically expose everything
```

Instead:

```text
Business requirement
        ↓
API contract
        ↓
Application model
        ↓
Persistence model
```

---

# 58. Backward Compatibility

Once an API is consumed by other systems:

```text
Client A
Client B
Client C
Mobile App
External Partner
```

changing:

```json
{
  "customerId": 1
}
```

into:

```json
{
  "customerName": "John"
}
```

can break consumers.

Therefore:

> API contracts are products that need lifecycle management.

---

# 59. Example Valid Order Request

Current project request format:

```json
{
  "productName": "Mechanical Keyboard",
  "quantity": 2,
  "customerId": 1,
  "status": "PENDING"
}
```

The important field is:

```text
customerId
```

The current API expects the customer relationship through the ID.

---

# 60. Invalid Request Example

If the request omits:

```text
customerId
```

validation should reject it.

Example error:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    {
      "field": "customerId",
      "message": "Customer ID must not be null"
    }
  ]
}
```

This behavior will be expanded during the validation lessons.

---

# 61. API Testing With PowerShell

Start the application:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"

.\mvnw.cmd spring-boot:run
```

Open another PowerShell window.

---

# 62. GET Orders

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders" `
  -Method GET
```

---

# 63. GET One Order

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/1" `
  -Method GET
```

If order 1 exists, you should receive the order.

If it does not:

```text
404 Not Found
```

is appropriate.

---

# 64. POST Order

PowerShell:

```powershell
$body = @{
    productName = "Mechanical Keyboard"
    quantity = 2
    customerId = 1
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

---

# 65. Why `ConvertTo-Json`?

PowerShell creates a PowerShell object:

```powershell
@{
    productName = "Mechanical Keyboard"
    quantity = 2
}
```

Spring expects JSON.

Therefore:

```text
PowerShell object
       ↓
ConvertTo-Json
       ↓
JSON
       ↓
HTTP request
```

---

# 66. PUT Order

Example:

```powershell
$body = @{
    productName = "Mechanical Keyboard"
    quantity = 5
    customerId = 1
    status = "CONFIRMED"
} | ConvertTo-Json
```

Then:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/1" `
  -Method PUT `
  -ContentType "application/json" `
  -Body $body
```

---

# 67. DELETE Order

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/1" `
  -Method DELETE
```

Depending on the current implementation, a successful deletion should use the appropriate success status.

---

# 68. Testing Invalid JSON

Example:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"productName":'
```

The API should reject malformed JSON.

The exact error response depends on the application's configured exception handling.

---

# 69. Testing Missing Fields

Example:

```powershell
$body = @{
    productName = "Keyboard"
    quantity = 2
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

Expected conceptually:

```text
400 Bad Request
```

because:

```text
customerId
```

is missing.

---

# 70. HTTP vs HTTPS

Development may use:

```text
http://localhost:8080
```

Production should normally use:

```text
https://...
```

HTTPS protects data in transit.

Later security lessons will cover:

```text
TLS
certificates
HTTPS
mTLS
certificate chains
```

---

# 71. HTTP Is Not the Application

HTTP is the communication protocol.

The application contains:

```text
Business logic
Domain rules
Persistence
Security
Transactions
```

Think:

```text
HTTP
 ↓
transport
 ↓
Application
```

Do not put business logic into HTTP-specific code unnecessarily.

---

# 72. REST API vs Database API

REST API:

```text
GET /orders/10
```

Database:

```sql
SELECT *
FROM orders
WHERE id = 10;
```

These are different abstractions.

The controller/service/repository architecture bridges them.

---

# 73. API Contract

An API contract defines:

```text
endpoint
method
request
response
status
error behavior
```

Example:

```text
POST /api/v1/orders

Request:
CreateOrderRequest

Success:
201 Created

Response:
OrderResponse

Invalid:
400 Bad Request

Missing resource:
404 Not Found
```

---

# 74. Contract-First Direction

Later the project will formalize this using:

```text
OpenAPI
```

The direction becomes:

```text
API Contract
      ↓
Generated/documented contract
      ↓
Implementation
      ↓
Tests
```

Day 2 establishes the conceptual foundation.

---

# 75. API Design Principles

Remember:

```text
1. Use nouns for resources.
2. Use HTTP methods intentionally.
3. Use status codes correctly.
4. Keep controllers thin.
5. Use DTOs at boundaries.
6. Validate input.
7. Don't expose internal details.
8. Preserve backward compatibility.
9. Design for idempotency where needed.
10. Keep APIs predictable.
```

---

# 76. KISS Applied to REST

Bad:

```text
POST /createNewOrder
POST /createOrderForCustomer
POST /order/create
```

Prefer:

```text
POST /orders
```

Simple and predictable.

---

# 77. YAGNI Applied to REST

Do not create:

```text
/api/v1/orders/export-to-xml
/api/v1/orders/advanced-analysis
/api/v1/orders/legacy-report
```

unless there is a real requirement.

Build what the system needs.

---

# 78. SOLID Applied to Controllers

Single Responsibility:

```text
OrderController
```

should primarily handle Order HTTP operations.

It should not become:

```text
OrderController
 ├── Customer business logic
 ├── Payment logic
 ├── SQL
 ├── Kafka
 ├── Redis
 └── email
```

---

# 79. Fail Fast Applied to APIs

Invalid input should be rejected early:

```text
HTTP request
 ↓
Validation
 ↓
Business logic
```

rather than:

```text
HTTP request
 ↓
Service
 ↓
Database
 ↓
failure
```

---

# 80. Security-Driven REST

Never trust client input.

A client can send:

```json
{
  "customerId": 999999999,
  "quantity": -500000
}
```

The server must validate and authorize independently.

Never rely on:

```text
Frontend validation
```

as a security boundary.

---

# 81. Least Privilege and APIs

Eventually an endpoint may require:

```text
ROLE_CUSTOMER
ROLE_SUPPORT
ROLE_ADMIN
ROLE_AUDITOR
```

Example:

```text
Customer
 ↓
Can view own orders

Admin
 ↓
Can manage orders
```

Authorization belongs on the server.

---

# 82. Reliability and HTTP

HTTP requests can fail.

Possible causes:

```text
network timeout
client retry
server timeout
database failure
load balancer failure
duplicate request
```

This is why:

```text
timeout
retry
idempotency
```

will become important later.

---

# 83. Observability

Every production request should eventually be traceable.

Future model:

```text
Request
 ↓
Correlation ID
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Database
```

Later:

```text
logs
metrics
traces
```

will be added.

---

# 84. Testing Strategy

Day 2 should verify API behavior.

Test:

```text
GET success
GET not found
POST success
POST invalid
PUT success
PUT invalid
DELETE success
DELETE not found
```

Also test:

```text
status codes
JSON response
validation
error response
```

---

# 85. Functional vs Performance Testing

Functional test:

```text
Does GET /orders return the correct data?
```

Performance test:

```text
How quickly does GET /orders respond under load?
```

Do not confuse the two.

Performance engineering comes later.

---

# 86. Build Verification

From PowerShell:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"

.\mvnw.cmd clean test
```

The goal is:

```text
Tests
Failures = 0
Errors = 0
```

---

# 87. Run Application

```powershell
.\mvnw.cmd spring-boot:run
```

Expected conceptually:

```text
Spring Boot started
Tomcat started
Application ready
```

The actual port depends on project configuration.

Current expected port:

```text
8080
```

---

# 88. Git Verification

Before finishing Day 2:

```powershell
git status
```

Review:

```powershell
git diff --stat
```

Then:

```powershell
git diff
```

---

# 89. Documentation

Create:

```text
docs/days/day-02.md
```

This document is the permanent Day 2 reference.

It should capture:

```text
REST
HTTP
CRUD
status codes
request/response
DTO
controller
API design
testing
commands
engineering principles
```

---

# 90. Git Commit

After testing:

```powershell
git add .
```

Review:

```powershell
git diff --cached
```

Commit:

```powershell
git commit -m "feat(api): establish rest api fundamentals"
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

# 91. Day 2 Definition of Done

```text
[ ] REST understood
[ ] HTTP understood
[ ] Resources understood
[ ] CRUD mapping understood
[ ] GET understood
[ ] POST understood
[ ] PUT understood
[ ] DELETE understood
[ ] Status codes understood
[ ] Path variables understood
[ ] Query parameters understood
[ ] Request bodies understood
[ ] JSON serialization understood
[ ] DTO concept understood
[ ] Controller responsibility understood
[ ] REST statelessness understood
[ ] Idempotency understood
[ ] API versioning introduced
[ ] Error handling understood
[ ] API tested
[ ] Maven tests pass
[ ] Documentation updated
[ ] Git commit created
[ ] GitHub push completed
```

---

# 92. Notebook Summary

## REST

```text
REST = architectural style
Resources = nouns
HTTP methods = operations
```

Example:

```text
GET    /orders
GET    /orders/1
POST   /orders
PUT    /orders/1
DELETE /orders/1
```

## HTTP

```text
Request
 ├── Method
 ├── URL
 ├── Headers
 └── Body

Response
 ├── Status
 ├── Headers
 └── Body
```

## Important Status Codes

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

## Spring MVC

```text
@RestController
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@PathVariable
@RequestParam
@RequestBody
```

## Architecture

```text
HTTP
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Database
```

## DTO

```text
Request JSON
 ↓
Request DTO
 ↓
Service
 ↓
Entity
 ↓
Database
```

and:

```text
Database
 ↓
Entity
 ↓
Response DTO
 ↓
JSON
```

## Idempotency

```text
PUT → generally idempotent
POST → not inherently idempotent
```

## Key Rule

> Keep the API predictable, controllers thin, contracts stable, and business logic outside the HTTP layer.

---

# 93. Day 2 Mental Model

```text
                         CLIENT
                            │
                            │ HTTP
                            ▼
                 ┌─────────────────────┐
                 │    REST CONTROLLER  │
                 │                     │
                 │ GET                 │
                 │ POST                │
                 │ PUT                 │
                 │ DELETE              │
                 └──────────┬──────────┘
                            │
                            │ DTO
                            ▼
                 ┌─────────────────────┐
                 │      SERVICE       │
                 │   Business Logic   │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │     REPOSITORY     │
                 │    Persistence     │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │     POSTGRESQL      │
                 └─────────────────────┘
```

---

# 94. Final Day 2 Lesson

The most important concept is not memorizing:

```text
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
```

The important concept is understanding the complete request lifecycle:

```text
Client
 ↓
HTTP Request
 ↓
REST Resource
 ↓
Controller
 ↓
DTO
 ↓
Service
 ↓
Repository
 ↓
Database
 ↓
Response DTO
 ↓
HTTP Response
 ↓
Client
```

A professional backend engineer understands the complete flow rather than only the controller annotation.

---

# 95. Progress

```text
Java Enterprise Backend Mastery

Day 02 / 365

Day 01 ✓ Project Foundation
Day 02 ✓ REST & HTTP Fundamentals

Next:
Day 03 → Domain Modeling & JPA Entity Design
```

The application now has the foundation for a real API. Every subsequent day should make this same API more robust, maintainable, secure, observable, reliable, and production-ready.
