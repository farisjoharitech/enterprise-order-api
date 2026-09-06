# Day 11 — JPA Fetching, N+1 and JOIN FETCH

## Project

`enterprise-order-api`

## Day Objective

Learn how JPA/Hibernate loads entity relationships and prevent inefficient database access when an `Order` needs its associated `Customer`.

---

# 1. Topics Learned

* `@ManyToOne`
* `FetchType.LAZY`
* `FetchType.EAGER`
* Persistence Context
* Transaction boundaries
* N+1 query problem
* JPQL
* `JOIN FETCH`
* `@EntityGraph` concept
* Explicit fetch strategies
* Entity-to-DTO mapping
* Repository-level performance optimization
* Collection endpoint testing
* Test isolation

---

# 2. Current Relationship

The project contains:

```text
Customer
    │
    │ 1
    │
    │
    │ *
Order
```

An `Order` belongs to one `Customer`.

The entity relationship is:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id", nullable = false)
private Customer customer;
```

---

# 3. LAZY Loading

`LAZY` means the related entity should not automatically be loaded simply because the parent entity was loaded.

Conceptually:

```text
Load Order
    ↓
Order data available
    ↓
Customer relationship available
    ↓
Customer data loaded when required
```

LAZY loading is useful because not every use case requires related data.

---

# 4. EAGER Loading

`EAGER` means the relationship is considered immediately required.

Example:

```java
@ManyToOne(fetch = FetchType.EAGER)
```

EAGER should not be used as a blanket solution to N+1 problems.

Different use cases require different amounts of data.

Prefer explicit fetch strategies at the repository/use-case level.

---

# 5. N+1 Query Problem

Suppose the application loads:

```text
100 orders
```

and then accesses each order's customer separately.

A naive implementation can result in:

```text
1 query
+
100 customer queries
=
101 queries
```

This is called the:

```text
N+1 Query Problem
```

Where:

```text
N = number of parent records
```

Examples:

```text
10 orders    → potentially 11 queries
100 orders   → potentially 101 queries
1,000 orders → potentially 1,001 queries
```

The performance impact becomes significant as data volume grows.

---

# 6. JOIN FETCH

The project adds a repository query:

```java
@Query("""
        SELECT o
        FROM Order o
        JOIN FETCH o.customer
        """)
List<Order> findAllWithCustomer();
```

`JOIN FETCH` tells Hibernate that the associated customer should be fetched as part of this query.

Conceptually:

```text
Order
  +
Customer
  ↓
JOIN FETCH
  ↓
Result
```

This allows a specific use case to retrieve the relationship efficiently without changing the global entity fetch strategy.

---

# 7. Why Not Change Everything to EAGER?

Changing:

```java
LAZY
```

to:

```java
EAGER
```

does not solve architectural problems.

Different endpoints may require different data.

Example:

```text
Use Case A
Order only

Use Case B
Order + Customer

Use Case C
Order + Customer + Account
```

A global EAGER strategy can load unnecessary data.

Better approach:

```text
Entity default
      ↓
LAZY
      ↓
Repository/use-case
      ↓
Explicit fetch strategy
```

---

# 8. Persistence Context

Hibernate maintains a persistence context during a transaction.

Conceptually:

```text
@Transactional
      │
      ▼
Persistence Context
      │
      ├── Order
      ├── Customer
      └── Entity state
```

The persistence context manages entity instances and their lifecycle during the transaction.

This is particularly important when working with LAZY relationships.

---

# 9. Transaction Boundary

The service method uses:

```java
@Transactional(readOnly = true)
public List<OrderResponse> getOrdersWithCustomers()
```

The service layer owns the transaction boundary.

Architecture:

```text
Controller
    ↓
Service
    ↓
@Transactional
    ↓
Repository
    ↓
Hibernate/JPA
    ↓
PostgreSQL
```

Controllers should not manage Hibernate persistence behavior directly.

---

# 10. New Day 11 Endpoint

Added:

```text
GET /api/v1/orders/with-customers
```

Purpose:

Demonstrate retrieving orders together with their associated customers using a fetch join.

Example:

```json
[
  {
    "id": 1,
    "customerId": 1,
    "customerName": "John",
    "productName": "Laptop",
    "quantity": 2,
    "status": "CREATED"
  }
]
```

---

# 11. Layer Responsibilities

## Controller

Handles HTTP:

```text
GET /api/v1/orders/with-customers
```

## Service

Handles application logic:

```java
getOrdersWithCustomers()
```

## Repository

Handles data access:

```java
findAllWithCustomer()
```

## Hibernate/JPA

Translates JPQL into SQL and manages entity state.

## PostgreSQL

Stores:

```text
orders
customers
```

---

# 12. Entity → DTO

The API does not directly expose the JPA entity.

Instead:

```text
Order entity
    ↓
OrderResponse
    ↓
HTTP response
```

This keeps the API contract separate from the database model.

---

# 13. Testing

Day 11 added/updated tests for:

### Repository

Tests the fetch-join repository method.

### Service

Tests:

```text
Service
   ↓
findAllWithCustomer()
   ↓
DTO mapping
```

### Controller

Tests:

```text
GET /api/v1/orders/with-customers
```

The controller test does not assume the expected order is `$[0]`.

Instead it verifies that the expected object exists somewhere in the returned collection.

This avoids coupling the test to database/test execution order.

---

# 14. Test Isolation Lesson

A collection endpoint may return data created by other tests.

Therefore this is fragile:

```java
jsonPath("$[0].customerName")
```

unless ordering is part of the API contract.

Better:

```text
Find the specific expected object
```

The test should verify the behavior it actually cares about.

---

# 15. @EntityGraph

Another JPA mechanism for controlling fetch plans is:

```java
@EntityGraph
```

Conceptually:

```java
@EntityGraph(attributePaths = "customer")
```

It can tell Spring Data JPA which relationship should be fetched for a repository operation.

Both approaches can solve similar use cases:

```text
JOIN FETCH
```

and:

```text
@EntityGraph
```

The important architectural lesson is not to use one blindly.

Choose the fetch strategy based on the query/use case.

---

# 16. Important Rules

### Rule 1

Prefer:

```java
FetchType.LAZY
```

as the default approach for relationships unless there is a clear reason otherwise.

### Rule 2

Do not solve every N+1 problem by changing relationships to EAGER.

### Rule 3

Use explicit fetch strategies for use cases that require related data.

### Rule 4

Be careful when accessing LAZY relationships outside a transaction.

### Rule 5

Keep transaction boundaries in the service layer.

### Rule 6

Keep database-specific fetching logic in repositories.

### Rule 7

Do not expose JPA entities directly from REST APIs.

### Rule 8

Do not assume collection ordering unless the API explicitly guarantees it.

---

# 17. Commands Used

Open project:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Compile:

```powershell
.\mvnw.cmd -DskipTests compile
```

Service tests:

```powershell
.\mvnw.cmd -Dtest=OrderServiceTest test
```

Controller tests:

```powershell
.\mvnw.cmd -Dtest=OrderControllerTests test
```

Application tests:

```powershell
.\mvnw.cmd -Dtest=EnterpriseOrderApiApplicationTests test
```

Start application:

```powershell
.\mvnw.cmd spring-boot:run
```

Check formatting:

```powershell
git diff --check
```

Check status:

```powershell
git status
```

Review changes:

```powershell
git diff
```

---

# 18. Testcontainers Note

`OrderRepositoryIntegrationTest` uses Testcontainers.

It requires a working Docker environment.

Docker is NOT a Day 11 learning topic.

If Docker is unavailable:

```text
Repository integration tests
        ↓
Skipped
```

This does not change the Day 11 JPA learning objective.

Docker/Testcontainers will be covered deliberately in the later containerization/DevOps portion of the roadmap.

---

# 19. Day 11 Architecture

```text
                    HTTP
                     │
                     ▼
              OrderController
                     │
                     ▼
                OrderService
                     │
              @Transactional
                     │
                     ▼
              OrderRepository
                     │
             JOIN FETCH customer
                     │
                     ▼
                 Hibernate
                     │
                     ▼
                PostgreSQL
```

---

# 20. What I Should Remember

The key mental model:

```text
LAZY
  ↓
Don't automatically load everything

N+1
  ↓
1 parent query + N relationship queries

JOIN FETCH
  ↓
Explicitly fetch required relationship

EntityGraph
  ↓
Declaratively define a fetch plan

@Transactional
  ↓
Define service transaction boundary

DTO
  ↓
Protect API contract from persistence model
```

---

# 21. Day 11 Completion Checklist

* [x] Understand `@ManyToOne`
* [x] Understand LAZY loading
* [x] Understand EAGER loading
* [x] Understand persistence context
* [x] Understand N+1
* [x] Implement `JOIN FETCH`
* [x] Add fetch-optimized repository method
* [x] Add service method
* [x] Add controller endpoint
* [x] Add repository test
* [x] Add service test
* [x] Add controller test
* [x] Fix test dependency on collection ordering
* [x] Run service tests
* [x] Run controller tests
* [x] Run application tests
* [ ] Repository integration tests require Docker/Testcontainers
* [ ] Review Git diff
* [ ] Commit Day 11
* [ ] Push Day 11

---

# 22. One-Sentence Interview Answer

> "I keep JPA relationships lazy by default and use explicit fetch strategies such as JOIN FETCH or EntityGraph for use cases that require related data, which helps avoid N+1 queries without globally switching relationships to EAGER."

---

# 23. Day 11 Result

The application now demonstrates an explicit JPA fetching strategy for:

```text
Order → Customer
```

without changing the global relationship to EAGER.

The project is ready to move from basic CRUD/querying toward more production-oriented persistence behavior.
