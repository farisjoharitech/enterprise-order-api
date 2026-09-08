# Java Enterprise Backend Mastery

# `enterprise-order-api`

## Days 1–15 Master Reference

---

# 1. Project Vision

The goal is to build **one evolving enterprise-grade Java backend application**.

No separate toy applications.

Every learning topic becomes part of the same system.

```text
Day 1
  ↓
Day 2
  ↓
Day 3
  ↓
...
  ↓
Day 15
  ↓
...
  ↓
Day 365
  ↓
Enterprise Banking Backend
```

The project begins as an Order API and progressively evolves toward an enterprise/banking-style backend incorporating:

* Java
* Spring Boot
* REST
* PostgreSQL
* JPA/Hibernate
* database design
* transactions
* concurrency
* security
* testing
* DDD
* event-driven architecture
* microservices
* Docker
* CI/CD
* Kubernetes
* cloud
* observability
* resilience
* AI integration

---

# 2. Current Project

Repository:

```text
enterprise-order-api
```

GitHub:

```text
https://github.com/farisjoharitech/enterprise-order-api
```

Local Windows path:

```text
C:\Users\User\Documents\JavaProjects\enterprise-order-api
```

Branch:

```text
main
```

Current technology foundation:

```text
Java 17
Spring Boot 4.1.1
Maven
Spring Web MVC
Spring Data JPA
Hibernate
PostgreSQL
Flyway
H2
Testcontainers
Bean Validation
Actuator
```

---

# 3. Core Development Rule

Every day follows:

```text
LEARN
  ↓
DESIGN
  ↓
IMPLEMENT
  ↓
TEST
  ↓
MEASURE
  ↓
DOCUMENT
  ↓
GIT COMMIT
  ↓
GIT PUSH
```

The final application is the accumulated result of all days.

---

# 4. Day 1 — Project & Java/Spring Boot Foundation

## Objective

Create the foundation of the enterprise backend.

The project establishes:

```text
Spring Boot
Java
Maven
REST API
Layered architecture
Git
Testing
```

Basic architecture:

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Database
```

---

# 5. Day 1 Architecture

The initial application follows separation of concerns.

```text
controller/
service/
repository/
entity/
dto/
exception/
```

Responsibilities:

### Controller

Handles:

* HTTP request
* HTTP response
* request mapping
* validation boundary

Should NOT contain business logic.

### Service

Handles:

* business logic
* orchestration
* transaction boundaries

### Repository

Handles:

* persistence
* database access

### Entity

Represents persistence/domain data.

### DTO

Represents API contracts.

---

# 6. Day 2 — REST API Fundamentals

The application exposes REST endpoints.

Core HTTP methods:

```text
GET
POST
PUT
DELETE
```

Order API:

```text
GET    /api/v1/orders
GET    /api/v1/orders/{id}
POST   /api/v1/orders
PUT    /api/v1/orders/{id}
DELETE /api/v1/orders/{id}
```

REST principle:

```text
Resource
  +
HTTP method
  +
HTTP status
```

---

# 7. HTTP Status Codes

Important statuses:

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

Do not use `200 OK` for every situation.

HTTP status should communicate the result correctly.

---

# 8. Day 3 — Domain Model

The project introduced the initial domain:

```text
Customer
Order
```

Relationship:

```text
Customer
   │
   │ 1
   │
   └───────────*
              Order
```

An order belongs to a customer.

---

# 9. Database Model

Customers:

```text
customers
-------------------------
id
name
email
```

Orders:

```text
orders
-------------------------
id
customer_name
product_name
quantity
status
customer_id
```

Important relationship:

```text
orders.customer_id
        ↓
customers.id
```

Foreign-key relationship:

```text
Order → Customer
```

---

# 10. Day 4 — Validation & DTOs

API input should not directly expose persistence entities.

Use DTOs.

Example:

```json
{
  "productName": "Mechanical Keyboard",
  "quantity": 2,
  "customerId": 1,
  "status": "PENDING"
}
```

Validation examples:

```text
@NotNull
@NotBlank
@Size
@Min
@Max
```

Validation should happen at system boundaries.

---

# 11. Day 5 — Exception Handling

The application introduced structured error handling.

Instead of returning arbitrary errors:

```text
NullPointerException
SQL error
stack trace
```

the API returns a consistent response.

Example:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "...",
  "path": "..."
}
```

Centralized exception handling provides:

```text
Consistent API
Cleaner controllers
Better clients
Better observability
```

---

# 12. Day 6 — Service & Repository Separation

The application reinforces:

```text
Controller
    ↓
Service
    ↓
Repository
```

Controller:

```text
HTTP responsibility
```

Service:

```text
Business responsibility
```

Repository:

```text
Persistence responsibility
```

This is SoC:

> Separation of Concerns.

---

# 13. Day 7 — JPA Fundamentals

JPA maps Java objects to database tables.

Conceptual mapping:

```text
Java Entity
    ↓
Hibernate
    ↓
SQL
    ↓
PostgreSQL
```

Example:

```java
@Entity
@Table(name = "orders")
public class Order {
}
```

Primary key:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

---

# 14. Persistence Context

Hibernate maintains a persistence context.

Conceptually:

```text
Database
   ↕
Hibernate Persistence Context
   ↕
Java Entities
```

This allows Hibernate to track entity state.

Important states:

```text
Transient
Persistent
Detached
Removed
```

---

# 15. Day 8 — Entity Relationships

Customer/order relationship:

```java
@ManyToOne
@JoinColumn(name = "customer_id")
private Customer customer;
```

The project uses:

```java
@ManyToOne(fetch = FetchType.LAZY)
```

Important concept:

> Prefer LAZY loading unless eager loading is intentionally required.

Why?

Because EAGER relationships can unexpectedly load additional data.

---

# 16. Day 9 — Database & JPA Integration

The application connects:

```text
Spring Boot
     ↓
Spring Data JPA
     ↓
Hibernate
     ↓
PostgreSQL
```

Repository:

```java
public interface OrderRepository
        extends JpaRepository<Order, Long> {
}
```

Spring Data provides common operations:

```text
save()
findById()
findAll()
delete()
existsById()
```

---

# 17. Day 10 — Querying, Filtering, Pagination & Sorting

This was a major database/API evolution.

The project added:

```text
GET /api/v1/orders/search
```

Supported query parameters:

```text
status
customerId
page
size
sort
```

Example:

```text
/api/v1/orders/search?status=PENDING&page=0&size=20&sort=id
```

---

# 18. Spring Data Derived Queries

Spring Data can derive SQL behavior from method names.

Example:

```java
findByStatus(...)
```

```java
findByCustomer_Id(...)
```

```java
findByCustomer_IdAndStatus(...)
```

The repository expresses database access intent.

---

# 19. Pagination

Spring Data:

```java
Pageable
```

and:

```java
Page<T>
```

Example concept:

```text
Page 0
Size 20
Sort id
```

Database should perform:

```text
filter
sort
paginate
```

rather than retrieving everything into Java.

---

# 20. Why `/search` Was Added

Existing endpoint:

```text
GET /api/v1/orders
```

was preserved.

Instead:

```text
GET /api/v1/orders/search
```

was added for advanced querying.

Reason:

> Avoid breaking existing API consumers.

This introduces:

```text
Backward compatibility
```

as an architectural concern.

---

# 21. Day 11 — JPA Fetching & N+1

Important concepts:

```text
LAZY
EAGER
JOIN FETCH
N+1
```

Potential N+1:

```text
1 query → orders

then:

1 query → customer 1
1 query → customer 2
1 query → customer 3
...
```

Total:

```text
1 + N queries
```

This can become a serious performance problem.

---

# 22. JOIN FETCH

The project introduced:

```java
@Query("""
    SELECT o
    FROM Order o
    JOIN FETCH o.customer
""")
```

Conceptually:

```text
Orders
  +
Customers
  ↓
one optimized query
```

But JOIN FETCH should be used intentionally.

Do not blindly fetch every relationship.

---

# 23. Entity → DTO

Database entities should not automatically become API responses.

Prefer:

```text
Entity
   ↓
Mapping
   ↓
DTO
   ↓
JSON
```

Benefits:

```text
API stability
Encapsulation
Security
Smaller responses
Reduced coupling
```

---

# 24. Day 12 — Dynamic Filtering

The project introduced:

```text
Spring Data JPA Specifications
```

Repository:

```java
JpaSpecificationExecutor<Order>
```

This allows dynamic queries.

Current filter concepts include:

```text
status
customerId
productName
minimum quantity
maximum quantity
```

Specifications can be composed:

```text
Specification A
      AND
Specification B
      AND
Specification C
```

---

# 25. Why Specifications?

Without Specifications:

```text
findByStatusAndCustomerIdAndProductName...
findByStatusAndCustomerId...
findByStatusAndQuantity...
...
```

This can produce a huge number of repository methods.

Specifications allow reusable query predicates.

---

# 26. Day 13 — Flyway

Database schema changes became version-controlled.

Migration structure:

```text
db/migration/

V1__baseline.sql
V2__add_created_at_to_orders.sql
V3__add_order_version.sql
```

Every schema change becomes code.

---

# 27. Database Migration Principle

Never rely on:

```text
"Run this SQL manually on production."
```

Instead:

```text
Migration
   ↓
Git
   ↓
CI/CD
   ↓
Deployment
   ↓
Database
```

This provides:

```text
Repeatability
Traceability
Version control
Automation
```

---

# 28. Day 13 — `created_at`

The orders table gained:

```text
created_at
```

Type:

```text
TIMESTAMP WITH TIME ZONE
```

Existing rows were backfilled.

Then:

```text
DEFAULT CURRENT_TIMESTAMP
```

and:

```text
NOT NULL
```

were introduced.

This demonstrates safe database evolution:

```text
Expand
  ↓
Migrate
  ↓
Verify
  ↓
Switch
  ↓
Contract
```

---

# 29. Database Evolution Principle

Avoid dangerous one-step migrations where possible.

For production systems:

```text
Old application
      ↓
Compatible schema
      ↓
New application
      ↓
Data migration
      ↓
Remove old structure later
```

This reduces deployment risk.

---

# 30. Legacy `customer_name`

The project intentionally retains:

```text
orders.customer_name
```

even though it also has:

```text
orders.customer_id
```

This is part of the migration strategy.

Do not casually delete it.

The intended future process is:

```text
Expand
 ↓
Dual-compatible model
 ↓
Migrate
 ↓
Verify
 ↓
Switch
 ↓
Contract
```

---

# 31. Day 14 — Transactions

Day 14 introduced:

```text
@Transactional
```

Transactions provide atomicity.

Conceptually:

```text
BEGIN
  operation A
  operation B
  operation C
COMMIT
```

If failure occurs:

```text
ROLLBACK
```

---

# 32. ACID

Transactions follow the ACID model:

```text
A — Atomicity
C — Consistency
I — Isolation
D — Durability
```

### Atomicity

All or nothing.

### Consistency

Database remains valid.

### Isolation

Concurrent operations behave according to isolation rules.

### Durability

Committed changes survive failures.

---

# 33. Transaction Boundary

A transaction should normally surround a meaningful business operation.

Conceptually:

```text
Controller
    ↓
Service
    ↓
@Transactional
    ↓
Repository
```

Transaction boundaries belong primarily at the service/business-operation level.

---

# 34. Read-Only Transactions

For read operations:

```java
@Transactional(readOnly = true)
```

can communicate intent.

Example:

```text
GET
 ↓
Service
 ↓
readOnly transaction
 ↓
Repository
```

Do not assume `readOnly` magically makes every query faster.

It expresses transaction semantics and can allow framework/database optimizations.

---

# 35. Day 14 — Optimistic Locking

The project introduced:

```java
@Version
private Long version;
```

Database:

```text
version BIGINT NOT NULL DEFAULT 0
```

Migration:

```text
V3__add_order_version.sql
```

---

# 36. Lost Update Problem

Without concurrency protection:

```text
User A reads order
User B reads order

A changes quantity → 5
B changes quantity → 10

A saves
B saves

A's update is lost
```

---

# 37. Optimistic Locking

With:

```java
@Version
```

Hibernate effectively checks:

```text
UPDATE order
SET ...
WHERE id = ?
AND version = ?
```

If another transaction already changed the entity:

```text
version mismatch
```

and the update fails.

---

# 38. Version Rule

Application code should NOT manually increment:

```java
version++;
```

Hibernate manages the version.

Think:

```text
Application
     ↓
Entity modification
     ↓
Hibernate
     ↓
Version management
```

---

# 39. Concurrency Test

Day 14 introduced a real concurrent transaction test using:

```text
CountDownLatch
TransactionTemplate
```

The test verifies:

```text
Concurrent update
      ↓
Optimistic locking
      ↓
One update wins
      ↓
Conflicting update fails
```

This is much better than simply asserting a field manually.

---

# 40. Day 15 — Database Indexing & Query Performance

The current topic is:

> Database Indexing & Query Performance.

Main philosophy:

```text
Measure First
```

Do not add indexes just because columns exist.

---

# 41. Index

An index provides an alternative access path to table data.

Without an appropriate index:

```text
Sequential Scan
```

Potentially:

```text
row 1
row 2
row 3
...
row N
```

With an index:

```text
Index
 ↓
matching rows
 ↓
table rows
```

---

# 42. B-Tree

PostgreSQL commonly uses B-tree indexes.

Useful for many operations:

```text
=
<
>
<=
>=
BETWEEN
ORDER BY
```

---

# 43. Sequential Scan

```text
Seq Scan
```

does not mean:

```text
BAD
```

A sequential scan may be the best choice when:

```text
table is small
OR
large percentage of rows match
OR
planner estimates scanning is cheaper
```

---

# 44. Index Scan

Possible plan:

```text
Index Scan
```

The database uses an index to locate matching rows.

Again:

```text
Index Scan ≠ automatically faster
```

Measure actual performance.

---

# 45. Bitmap Scan

PostgreSQL may use:

```text
Bitmap Index Scan
      ↓
Bitmap Heap Scan
```

Useful when many rows/pages need to be retrieved.

---

# 46. EXPLAIN

Use:

```sql
EXPLAIN
SELECT *
FROM orders
WHERE customer_id = 1;
```

It shows the estimated execution plan.

---

# 47. EXPLAIN ANALYZE

Use:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1;
```

This executes the query and provides actual information.

Important:

```text
estimated
vs
actual
```

---

# 48. Query Planner

PostgreSQL chooses an execution plan.

Conceptually:

```text
SQL
 ↓
Parser
 ↓
Planner
 ↓
Cost estimation
 ↓
Execution plan
 ↓
Execution
```

Possible choices:

```text
Seq Scan
Index Scan
Bitmap Scan
Join strategies
Sort
Aggregate
```

---

# 49. Selectivity

Selectivity describes how much a condition narrows results.

Example:

```text
1,000,000 rows
      ↓
customer_id = 123
      ↓
100 rows
```

High selectivity.

Another:

```text
1,000,000 rows
      ↓
status = PENDING
      ↓
800,000 rows
```

Low selectivity.

---

# 50. Cardinality

Cardinality = number of distinct values.

Example:

```text
status
------
PENDING
CONFIRMED
CANCELLED
COMPLETED
```

Low cardinality.

`customer_id` normally has much higher cardinality.

---

# 51. Composite Index

Candidate for the existing application query:

```java
findByCustomer_IdAndStatus(...)
```

Potential index:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

Query:

```sql
SELECT *
FROM orders
WHERE customer_id = 1
AND status = 'PENDING';
```

---

# 52. Composite Index Column Order

These are different:

```text
(customer_id, status)
```

and:

```text
(status, customer_id)
```

Leading columns matter.

General mental model:

```text
(A, B)
 ↓
A
A + B
```

is generally more naturally supported than:

```text
B only
```

---

# 53. Existing Indexes

The project already has an index for:

```text
customer_id
```

Therefore do not create another equivalent index.

Avoid:

```text
duplicate indexes
```

---

# 54. Index Tradeoff

Indexes improve some reads but cost:

```text
storage
INSERT performance
UPDATE performance
DELETE performance
maintenance
vacuum work
migration time
```

Therefore:

```text
Indexes are a tradeoff.
```

---

# 55. Pagination Performance

The project already supports:

```text
page
size
sort
```

Large OFFSET values can become expensive:

```sql
LIMIT 20 OFFSET 10000
```

Conceptually:

```text
skip many rows
      ↓
return 20
```

Later topic:

```text
Keyset / Cursor Pagination
```

---

# 56. Database Performance Workflow

The professional workflow:

```text
1. Identify slow query
2. Reproduce
3. Measure
4. EXPLAIN
5. EXPLAIN ANALYZE
6. Inspect buffers
7. Understand workload
8. Design optimization
9. Implement
10. Measure again
11. Compare
12. Verify correctness
13. Monitor
```

---

# 57. Current Database Schema

Conceptual current schema:

```text
CUSTOMERS
────────────────────────
id       PK
name
email    UNIQUE


ORDERS
────────────────────────
id             PK
customer_name
product_name
quantity
status
customer_id    FK
created_at
version
```

Relationship:

```text
CUSTOMERS
    │
    │ 1
    │
    │
    │ *
ORDERS
```

---

# 58. Current Migrations

```text
V1__baseline.sql
        ↓
V2__add_created_at_to_orders.sql
        ↓
V3__add_order_version.sql
        ↓
V4__add_order_query_indexes.sql
```

V4 is only justified when measurement supports the index.

---

# 59. Current API

Base URL:

```text
http://localhost:8080/api/v1
```

Orders:

```text
GET    /orders
GET    /orders/{id}
POST   /orders
PUT    /orders/{id}
DELETE /orders/{id}
GET    /orders/search
```

---

# 60. Example Order Request

```json
{
  "productName": "Mechanical Keyboard",
  "quantity": 2,
  "customerId": 1,
  "status": "PENDING"
}
```

---

# 61. Search Example

```text
GET /api/v1/orders/search
```

Parameters:

```text
status
customerId
page
size
sort
```

Example:

```text
/api/v1/orders/search?customerId=1&status=PENDING&page=0&size=20&sort=id
```

---

# 62. Engineering Principles

These principles apply throughout the project.

## SOLID

```text
S — Single Responsibility
O — Open/Closed
L — Liskov Substitution
I — Interface Segregation
D — Dependency Inversion
```

Question:

> Is this design maintainable?

---

# 63. DRY

Don't duplicate knowledge.

Bad:

```text
same validation
same mapping
same query logic
same business rule
```

in multiple locations.

But don't over-abstract prematurely.

---

# 64. KISS

Keep it simple.

Ask:

> Can this be simpler?

Avoid unnecessary:

```text
patterns
factories
interfaces
frameworks
abstractions
```

---

# 65. YAGNI

You Aren't Gonna Need It.

Do not build:

```text
future feature
hypothetical abstraction
unused configuration
unused index
```

before there is a real need.

---

# 66. Separation of Concerns

Each layer has a purpose.

```text
Controller → HTTP
Service    → Business logic
Repository → Persistence
Entity     → Domain/persistence model
DTO        → API contract
Migration  → Database schema
```

---

# 67. High Cohesion

A class should have a focused responsibility.

Bad:

```text
OrderService
 ├── business logic
 ├── SQL
 ├── email
 ├── logging infrastructure
 ├── HTTP handling
 └── database migration
```

Better:

```text
Controller
Service
Repository
Infrastructure
```

---

# 68. Low Coupling

Minimize unnecessary dependencies.

Example:

```text
Controller
   ↓
Service
   ↓
Repository
```

rather than:

```text
Controller
 ├── Repository
 ├── Database
 ├── Email
 ├── Kafka
 └── Redis
```

---

# 69. Encapsulation

Hide implementation details.

For example:

```text
Controller
```

should not need to know:

```text
Hibernate implementation details
SQL execution details
transaction internals
```

---

# 70. Composition

Prefer composition over unnecessary inheritance.

Think:

```text
OrderService
  has dependencies
```

rather than building deep inheritance trees.

---

# 71. Fail Fast

Validate bad input early.

Example:

```text
HTTP request
 ↓
Validation
 ↓
Business logic
```

rather than:

```text
invalid request
 ↓
database
 ↓
database error
```

---

# 72. Least Privilege

Components should receive only the permissions they need.

This becomes particularly important later with:

```text
database users
service accounts
cloud IAM
Kubernetes
API authorization
```

---

# 73. Design for Failure

Always ask:

> What happens if this dependency fails?

Examples:

```text
Database unavailable
Redis unavailable
Kafka unavailable
External API timeout
Network failure
```

This becomes increasingly important in later days.

---

# 74. Idempotency

Ask:

> What happens if this operation executes twice?

Important for:

```text
payments
orders
messages
retries
API requests
event processing
```

This becomes critical when the project evolves toward banking.

---

# 75. Observability

Ask:

> How will I know what is happening in production?

Eventually:

```text
Logs
Metrics
Traces
Health checks
Alerts
Dashboards
```

Day 15's `EXPLAIN ANALYZE` is an example of measurement at the database level.

---

# 76. TDD

Test-Driven Development:

```text
RED
 ↓
GREEN
 ↓
REFACTOR
```

Meaning:

### RED

Write failing test.

### GREEN

Implement minimum code.

### REFACTOR

Improve design while preserving behavior.

---

# 77. DDD

Domain-Driven Design is applied progressively.

Current domain:

```text
Customer
Order
```

Future banking domain may include:

```text
Customer
Account
Transaction
Payment
Transfer
Card
Loan
Audit
Risk
```

DDD will become deeper later:

```text
Entities
Value Objects
Aggregates
Repositories
Domain Services
Domain Events
Bounded Contexts
```

---

# 78. Contract-First

API contracts should increasingly be defined before implementation.

Concept:

```text
API Contract
    ↓
Implementation
    ↓
Tests
```

Future:

```text
OpenAPI
JSON Schema
Event Schema
Consumer Contracts
```

---

# 79. Event-Driven Development

Later the project will introduce events where justified.

Example:

```text
OrderCreated
OrderUpdated
OrderCancelled
```

Architecture:

```text
Business action
      ↓
Domain event
      ↓
Integration event
      ↓
Message broker
```

Do not introduce messaging simply for the sake of using Kafka.

---

# 80. Security-Driven Development

Security is considered during feature design.

Questions:

```text
Who can perform this action?
What data can they access?
Can input be trusted?
Can requests be replayed?
Can secrets leak?
Is authorization enforced server-side?
```

Future:

```text
Authentication
Authorization
JWT
OAuth2/OIDC
RBAC
ABAC
Audit
Secrets
Encryption
```

---

# 81. Reliability-Driven Development

For each dependency:

```text
What if it is slow?
What if it times out?
What if it fails?
What if it returns invalid data?
What if the request is retried?
```

Future patterns:

```text
Timeout
Retry
Circuit Breaker
Bulkhead
Rate Limit
Idempotency
Dead Letter Queue
```

---

# 82. DevOps-Driven Development

The application will eventually be deployable through:

```text
Git
 ↓
Build
 ↓
Test
 ↓
Package
 ↓
Docker
 ↓
CI/CD
 ↓
Deployment
 ↓
Monitoring
```

Future tooling includes:

```text
GitHub Actions
Jenkins
Docker
Kubernetes
Cloud
```

---

# 83. Windows Daily Commands

## Go to project

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

## Check location

```powershell
Get-Location
```

## Git status

```powershell
git status
```

## Recent commits

```powershell
git log --oneline -8
```

---

# 84. Build & Test

Run all tests:

```powershell
.\mvnw.cmd clean test
```

Run tests without clean:

```powershell
.\mvnw.cmd test
```

Compile:

```powershell
.\mvnw.cmd -DskipTests compile
```

Run application:

```powershell
.\mvnw.cmd spring-boot:run
```

---

# 85. PostgreSQL

Connect:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
  -h localhost `
  -p 5433 `
  -U postgres `
  -d enterprise_order_db
```

Database:

```text
enterprise_order_db
```

Host:

```text
localhost
```

Port:

```text
5433
```

---

# 86. PostgreSQL Useful Commands

Show tables:

```sql
\dt
```

Describe orders:

```sql
\d orders
```

Describe customers:

```sql
\d customers
```

Count orders:

```sql
SELECT COUNT(*)
FROM orders;
```

Count customers:

```sql
SELECT COUNT(*)
FROM customers;
```

---

# 87. Check Order Indexes

```sql
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'orders'
ORDER BY indexname;
```

---

# 88. Analyze Data Distribution

Status:

```sql
SELECT
    status,
    COUNT(*) AS total
FROM orders
GROUP BY status
ORDER BY total DESC;
```

Orders per customer:

```sql
SELECT
    customer_id,
    COUNT(*) AS total_orders
FROM orders
GROUP BY customer_id
ORDER BY total_orders DESC
LIMIT 10;
```

---

# 89. PostgreSQL Query Performance

Customer:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1;
```

Status:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE status = 'PENDING';
```

Combined:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1
AND status = 'PENDING';
```

Pagination:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1
AND status = 'PENDING'
ORDER BY id
LIMIT 20
OFFSET 0;
```

---

# 90. API Testing From PowerShell

Get orders:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders" `
  -Method GET
```

Search:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?status=PENDING&page=0&size=20&sort=id" `
  -Method GET
```

Customer + status:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?customerId=1&status=PENDING&page=0&size=20&sort=id" `
  -Method GET
```

---

# 91. Git Daily Workflow

Before work:

```powershell
git status
git log --oneline -8
```

After implementation:

```powershell
git status
git diff --stat
git diff
```

Stage:

```powershell
git add .
```

Review staged changes:

```powershell
git diff --cached --stat
git diff --cached
```

Commit:

```powershell
git commit -m "feat: ..."
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

# 92. Daily Documentation Convention

Every day:

```text
docs/days/day-XX.md
```

Example:

```text
docs/days/day-15.md
```

Each document should contain:

```text
Objective
Theory
Architecture
Implementation
Tests
Commands
Problems
Solutions
Design decisions
Engineering principles
Lessons learned
Git commit
```

This creates a permanent personal reference library.

---

# 93. The Project's Architecture After Day 15

```text
                         CLIENT
                           │
                           ▼
                    REST Controller
                           │
                    validation / DTO
                           │
                           ▼
                       Service
                           │
                    transaction boundary
                           │
                           ▼
                     Repository
                           │
                           ▼
                     JPA / Hibernate
                           │
                           ▼
                         SQL
                           │
                 ┌─────────┴──────────┐
                 ▼                    ▼
          Query Planner          Transactions
                 │                    │
                 ▼                    ▼
              Indexes          Optimistic Lock
                 │                    │
                 └─────────┬──────────┘
                           ▼
                       PostgreSQL
```

---

# 94. Evolution From Day 1 → Day 15

```text
DAY 1
Project foundation
       ↓
DAY 2
REST API
       ↓
DAY 3
Domain model
       ↓
DAY 4
DTO + validation
       ↓
DAY 5
Exception handling
       ↓
DAY 6
Service/repository separation
       ↓
DAY 7
JPA fundamentals
       ↓
DAY 8
Entity relationships
       ↓
DAY 9
Database integration
       ↓
DAY 10
Query/filter/pagination/sorting
       ↓
DAY 11
N+1/JOIN FETCH
       ↓
DAY 12
Dynamic Specifications
       ↓
DAY 13
Flyway/schema evolution
       ↓
DAY 14
Transactions/concurrency/optimistic locking
       ↓
DAY 15
Indexes/query performance
```

---

# 95. Most Important Lessons From Days 1–15

## Lesson 1

Architecture is about responsibilities.

```text
Controller
Service
Repository
Database
```

---

## Lesson 2

JPA does not replace SQL knowledge.

You still need to understand:

```text
SQL
Indexes
Joins
Transactions
Query plans
Locks
```

---

## Lesson 3

Database schema is code.

Use:

```text
Flyway
+
Git
```

---

## Lesson 4

Transactions belong around business operations.

```text
Business operation
      ↓
Transaction
```

---

## Lesson 5

Concurrency must be designed explicitly.

```text
Concurrent users
      ↓
Potential lost update
      ↓
Optimistic locking
```

---

## Lesson 6

Indexes must be evidence-driven.

```text
Measure
 ↓
Understand
 ↓
Optimize
 ↓
Measure again
```

---

## Lesson 7

Performance is a system property.

```text
Java
 +
JPA
 +
Hibernate
 +
SQL
 +
PostgreSQL
```

all affect performance.

---

## Lesson 8

Backward compatibility matters.

Do not casually change an existing API simply because a new design looks cleaner.

---

## Lesson 9

Simple is better than unnecessarily clever.

```text
KISS
+
YAGNI
```

---

## Lesson 10

Enterprise engineering is mostly about managing tradeoffs.

Examples:

```text
Performance vs simplicity
Consistency vs availability
Read performance vs write cost
Abstraction vs complexity
Security vs convenience
Flexibility vs maintainability
```

---

# 96. Day 15 Interview Mental Model

When asked:

> "How would you optimize a slow database query?"

Answer:

```text
I would first reproduce the problem and measure it.

Then I would inspect the SQL and use EXPLAIN
and EXPLAIN ANALYZE to understand the execution plan.

I would inspect row counts, selectivity, indexes,
statistics and query patterns.

Only after identifying the bottleneck would I
consider an index or query change.

After the change I would measure again and verify
that functional behavior remains correct.

I would also consider the write and maintenance
cost of any new indexes.
```

---

# 97. Senior-Level Mental Model

Do not think:

```text
Java developer
```

Think:

```text
Software engineer
        +
Backend engineer
        +
Database engineer
        +
Security engineer
        +
Reliability engineer
        +
DevOps engineer
        +
System designer
```

The project is deliberately teaching all of these progressively.

---

# 98. Day 1–15 Final Checklist

```text
[✓] Java/Spring Boot foundation
[✓] Maven
[✓] Git
[✓] REST
[✓] HTTP
[✓] DTO
[✓] Validation
[✓] Exception handling
[✓] Layered architecture
[✓] JPA
[✓] Hibernate
[✓] Entity relationships
[✓] PostgreSQL
[✓] Spring Data repositories
[✓] Derived queries
[✓] Filtering
[✓] Pagination
[✓] Sorting
[✓] JOIN FETCH
[✓] N+1 awareness
[✓] Specifications
[✓] Flyway
[✓] Schema evolution
[✓] Transactions
[✓] ACID
[✓] Transaction boundaries
[✓] Optimistic locking
[✓] @Version
[✓] Concurrency testing
[✓] Database indexes
[✓] EXPLAIN
[✓] EXPLAIN ANALYZE
[✓] Query planner
[✓] Selectivity
[✓] Cardinality
[✓] Composite indexes
[✓] Performance measurement
```

---

# 99. The Golden Rules

```text
1. Keep one evolving project.

2. Learn → implement → test → document → commit.

3. Never bypass tests.

4. Never change production-style schema manually
   when the change belongs in Flyway.

5. Don't create abstractions without a reason.

6. Don't create indexes without evidence.

7. Don't expose entities unnecessarily through APIs.

8. Keep controllers thin.

9. Keep business logic in services/domain.

10. Keep persistence logic in repositories.

11. Treat concurrency as a first-class concern.

12. Treat security as a first-class concern.

13. Treat observability as a first-class concern.

14. Design for failure.

15. Measure before optimizing.

16. Prefer simple solutions until complexity is justified.

17. Preserve backward compatibility deliberately.

18. Every day's work must contribute to the final system.
```

---

# 100. Project Direction After Day 15

The next progression is:

```text
Day 15
Database Indexing
      ↓
Day 16
Advanced JPA & Hibernate Performance
      ↓
Day 17
Advanced Database Query Optimization
      ↓
Day 18
Advanced Database Evolution
      ↓
Day 19
Contract-First API
      ↓
Day 20
API Versioning
      ↓
Day 21+
Security
      ↓
Authentication
      ↓
Authorization
      ↓
JWT
      ↓
Caching
      ↓
Redis
      ↓
Resilience
      ↓
Messaging
      ↓
Kafka/RabbitMQ
      ↓
Event-driven architecture
      ↓
Outbox
      ↓
DDD
      ↓
Microservices
      ↓
Docker
      ↓
CI/CD
      ↓
Kubernetes
      ↓
Cloud
      ↓
Observability
      ↓
Distributed systems
      ↓
AI integration
      ↓
Enterprise Banking Platform
```

---

# Final Mental Model

The entire first 15 days can be remembered as:

```text
              ENTERPRISE BACKEND
                     │
                     ▼
              REST API / DTO
                     │
                     ▼
                 SERVICE
                     │
             TRANSACTION
                     │
                     ▼
                REPOSITORY
                     │
                     ▼
             JPA / HIBERNATE
                     │
              ┌──────┴──────┐
              ▼             ▼
           SQL/JPA      Persistence
              │           Context
              ▼
          PostgreSQL
              │
       ┌──────┼──────┐
       ▼      ▼      ▼
    Queries  Locks  Indexes
       │      │      │
       └──────┼──────┘
              ▼
          Performance
```

The fundamental engineering loop is:

```text
BUILD
 ↓
TEST
 ↓
MEASURE
 ↓
UNDERSTAND
 ↓
IMPROVE
 ↓
DOCUMENT
 ↓
COMMIT
```

And the fundamental architectural rule is:

```text
SIMPLE
+
CORRECT
+
TESTED
+
MEASURED
+
SECURE
+
OBSERVABLE
+
RELIABLE
```

That is the foundation established by Days 1–15.
