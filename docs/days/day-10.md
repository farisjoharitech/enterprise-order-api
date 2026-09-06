# Day 10 — Spring Data JPA Querying, Filtering, Pagination & Sorting

**Project:** `enterprise-order-api`
**Day:** 10 of 120
**Focus:** Spring Data JPA querying, filtering, pagination, sorting, API compatibility, and testing

---

# 1. Day 10 Objective

The goal of Day 10 was to evolve the Order API from basic CRUD into a searchable and pageable enterprise-style API.

We implemented:

* Spring Data JPA derived queries
* Status filtering
* Customer filtering
* Combined filtering
* Pagination
* Sorting
* `Pageable`
* `Page<T>`
* Custom pagination response DTO
* Database-side filtering
* Repository query responsibility
* Service-layer query orchestration
* Controller query parameters
* Repository integration tests
* Service unit tests
* Controller integration tests
* Empty-result handling
* API compatibility preservation

The existing Order API was deliberately preserved while a new search endpoint was introduced.

---

# 2. Project Architecture Before Day 10

The application already had the following structure:

```text
Client
   |
   v
OrderController
   |
   v
OrderService
   |
   v
OrderRepository
   |
   v
Spring Data JPA
   |
   v
Hibernate
   |
   v
PostgreSQL
```

Day 10 extended this architecture with database-backed querying.

---

# 3. Day 10 Architecture

```text
                         Client
                           |
                           | HTTP
                           v
                  +-------------------+
                  |  OrderController  |
                  |                   |
                  | status            |
                  | customerId        |
                  | Pageable          |
                  +---------+---------+
                            |
                            v
                  +-------------------+
                  |   OrderService    |
                  |                   |
                  | Query selection   |
                  | DTO mapping       |
                  +---------+---------+
                            |
                            v
                  +-------------------+
                  | OrderRepository   |
                  |                   |
                  | Derived Queries   |
                  | Page<Order>       |
                  +---------+---------+
                            |
                            v
                     Spring Data JPA
                            |
                            v
                         Hibernate
                            |
                            v
                       PostgreSQL
```

---

# 4. Why We Added `/search`

The existing endpoint was:

```http
GET /api/v1/orders
```

It returned a list:

```json
[
  {
    "id": 1,
    "customerId": 1,
    "customerName": "Faris",
    "productName": "Keyboard",
    "quantity": 2,
    "status": "CREATED"
  }
]
```

Changing it directly to:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

would be a breaking API contract change.

Therefore Day 10 introduced:

```http
GET /api/v1/orders/search
```

while keeping:

```http
GET /api/v1/orders
```

unchanged.

This demonstrates an important enterprise API principle:

> Adding new functionality should not unnecessarily break existing consumers.

---

# 5. New Search Endpoint

The new endpoint is:

```http
GET /api/v1/orders/search
```

It supports:

```text
status
customerId
page
size
sort
```

---

# 6. Search Without Filters

Request:

```http
GET /api/v1/orders/search
```

Default configuration:

```text
page = 0
size = 20
sort = id DESC
```

Example response:

```json
{
  "content": [
    {
      "id": 10,
      "customerId": 1,
      "customerName": "Faris",
      "productName": "Keyboard",
      "quantity": 2,
      "status": "CREATED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

# 7. Status Filtering

Request:

```http
GET /api/v1/orders/search?status=CREATED
```

This searches only orders where:

```text
status = CREATED
```

The repository method is:

```java
Page<Order> findByStatus(
        String status,
        Pageable pageable
);
```

Spring Data derives the database query from the method name.

Conceptually:

```sql
SELECT *
FROM orders
WHERE status = ?
```

---

# 8. Customer Filtering

Request:

```http
GET /api/v1/orders/search?customerId=1
```

Repository method:

```java
Page<Order> findByCustomer_Id(
        Long customerId,
        Pageable pageable
);
```

The important part is:

```text
Customer_Id
```

The Order entity contains:

```java
private Customer customer;
```

and Customer contains:

```java
private Long id;
```

Therefore:

```text
findByCustomer_Id()
```

means:

```text
Order.customer.id
```

Conceptually:

```sql
SELECT *
FROM orders
WHERE customer_id = ?
```

---

# 9. Combined Filtering

Request:

```http
GET /api/v1/orders/search?customerId=1&status=CREATED
```

Repository method:

```java
Page<Order> findByCustomer_IdAndStatus(
        Long customerId,
        String status,
        Pageable pageable
);
```

Conceptually:

```sql
SELECT *
FROM orders
WHERE customer_id = ?
AND status = ?
```

The service selects this method when both filters are supplied.

---

# 10. Query Selection

The service uses four cases.

```text
customerId + status
        |
        v
findByCustomer_IdAndStatus()

customerId only
        |
        v
findByCustomer_Id()

status only
        |
        v
findByStatus()

no filters
        |
        v
findAll(Pageable)
```

This is intentionally simple.

More advanced dynamic querying will be useful when the search requirements become more complicated.

---

# 11. Pageable

Spring provides the `Pageable` abstraction for pagination and sorting.

Example:

```http
GET /api/v1/orders/search?page=0&size=10&sort=id,desc
```

This becomes conceptually:

```text
Pageable
 |
 +-- page = 0
 |
 +-- size = 10
 |
 +-- sort = id DESC
```

The controller does not manually parse these parameters.

Spring MVC handles conversion into a `Pageable`.

---

# 12. `@PageableDefault`

The controller uses:

```java
@PageableDefault(
        size = 20,
        sort = "id",
        direction = Sort.Direction.DESC
)
Pageable pageable
```

This means that when the client does not specify pagination:

```text
page = 0
size = 20
sort = id DESC
```

are used.

The client can override them.

---

# 13. Pagination Example

Request:

```http
GET /api/v1/orders/search?page=0&size=10
```

The application requests:

```text
Page 0
10 records
```

If there are 35 records:

```text
totalElements = 35
totalPages = 4
```

because:

```text
35 / 10 = 3.5
```

which requires four pages.

---

# 14. Sorting

Example:

```http
GET /api/v1/orders/search?page=0&size=10&sort=id,desc
```

Sorting means:

```text
id DESC
```

Ascending:

```http
GET /api/v1/orders/search?page=0&size=10&sort=id,asc
```

The database performs the ordering.

Conceptually:

```sql
ORDER BY id DESC
```

---

# 15. Combined Search

The complete query can contain all features:

```http
GET /api/v1/orders/search?customerId=1&status=CREATED&page=0&size=10&sort=id,desc
```

This represents:

```text
Customer:
    1

Status:
    CREATED

Page:
    0

Size:
    10

Sort:
    id DESC
```

---

# 16. `Page<Order>`

Spring Data repositories can return:

```java
Page<Order>
```

A Page contains both the data and pagination information.

Important methods include:

```java
getContent()
getNumber()
getSize()
getTotalElements()
getTotalPages()
isFirst()
isLast()
```

---

# 17. Why We Created `OrderPageResponse`

Instead of exposing Spring's internal `Page` implementation directly, the application converts the result into:

```java
OrderPageResponse
```

The DTO contains:

```java
public record OrderPageResponse(
        List<OrderResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
```

This keeps the REST API contract under our application's control.

---

# 18. DTO Mapping

The architecture remains:

```text
Order Entity
     |
     v
OrderService
     |
     v
OrderResponse
```

For search:

```text
Page<Order>
     |
     v
Page content
     |
     v
OrderResponse
     |
     v
OrderPageResponse
```

We do not expose JPA entities directly through the controller.

---

# 19. Database-Side Filtering

A poor implementation would be:

```text
SELECT all orders
        |
        v
Java application
        |
        v
filter()
        |
        v
sort()
        |
        v
paginate()
```

This becomes increasingly expensive as the database grows.

The preferred approach is:

```text
PostgreSQL
    |
    +-- WHERE
    +-- ORDER BY
    +-- LIMIT
    +-- OFFSET
    |
    v
Only required data
```

This reduces unnecessary:

* database-to-application data transfer
* memory usage
* Java processing
* network traffic

---

# 20. Derived Queries

Spring Data JPA allows repository methods to describe queries through method names.

Example:

```java
findByStatus(...)
```

Example:

```java
findByCustomer_Id(...)
```

Example:

```java
findByCustomer_IdAndStatus(...)
```

Spring Data parses these method names and creates the required query.

This eliminates unnecessary SQL for simple queries.

---

# 21. Query Strategy

Our preferred query strategy is:

```text
1. Derived query
        |
        v
2. JPQL @Query
        |
        v
3. Native SQL
```

Use the simplest appropriate mechanism.

For Day 10, derived queries are sufficient.

---

# 22. Why We Did Not Use Native SQL

There is no need to write:

```java
@Query(
    value = "...",
    nativeQuery = true
)
```

for the current requirements.

Spring Data already understands:

```java
findByStatus()
```

and:

```java
findByCustomer_IdAndStatus()
```

Using the higher-level abstraction keeps the code simpler and easier to maintain.

---

# 23. Repository Responsibility

The repository should focus on:

```text
Data access
Database queries
Persistence
```

It should not contain:

```text
HTTP logic
REST responses
HTTP status handling
Business workflow
```

---

# 24. Service Responsibility

The service handles:

```text
Query selection
Application logic
DTO mapping
Transaction boundaries
```

For example:

```text
customerId + status
       |
       v
select combined repository query
```

---

# 25. Controller Responsibility

The controller handles:

```text
HTTP endpoint
Request parameters
Pageable
REST response
```

The controller does not contain database logic.

---

# 26. Empty Search Results

A search that finds nothing is normally successful.

Example:

```http
GET /api/v1/orders/search?customerId=999999999
```

Expected:

```http
200 OK
```

with:

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0
}
```

It should not normally become:

```http
404 Not Found
```

because the search endpoint itself exists and successfully executed.

---

# 27. Testing Strategy

Day 10 uses multiple testing levels.

```text
Controller Tests
       |
       v
HTTP/API behavior

Service Tests
       |
       v
Query-selection logic

Repository Integration Tests
       |
       v
Actual JPA/database behavior
```

This gives better coverage than relying on one test type.

---

# 28. Controller Tests

The controller tests cover:

* status filtering
* customer filtering
* combined filtering
* pagination
* empty results

An important problem was discovered during testing.

The original status-filter test expected:

```text
totalElements = 1
```

but received:

```text
totalElements = 14
```

---

# 29. Why the Test Initially Failed

The integration test database contained data created by other tests.

Therefore:

```text
Our test order
      +
Existing CREATED orders
      =
14 CREATED orders
```

The production code was working correctly.

The test was making an incorrect assumption:

```text
"Only my test data exists."
```

---

# 30. Test Isolation Lesson

Instead of searching all:

```text
status = CREATED
```

the test was changed to use its own customer:

```text
customerId = testCustomer
AND
status = CREATED
```

Now the test can reliably identify its own data.

This is a useful integration-testing principle:

> Tests should avoid depending on unrelated shared database state.

---

# 31. Repository Integration Tests

Repository tests verify:

```text
findByStatus()
findByCustomer_Id()
findByCustomer_IdAndStatus()
findAll(Pageable)
```

They also verify:

```text
pagination
sorting
totalElements
totalPages
```

These tests validate actual Spring Data JPA behavior.

---

# 32. Service Unit Tests

Service tests verify that the service chooses the correct repository method.

Example:

```text
status only
    ↓
findByStatus()

customerId only
    ↓
findByCustomer_Id()

customerId + status
    ↓
findByCustomer_IdAndStatus()

no filters
    ↓
findAll(Pageable)
```

This isolates service behavior from the actual database.

---

# 33. Maven Commands Used

Navigate to the project:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Compile without tests:

```powershell
.\mvnw.cmd -DskipTests compile
```

Run controller tests:

```powershell
.\mvnw.cmd -Dtest=OrderControllerTests test
```

Run service tests:

```powershell
.\mvnw.cmd -Dtest=OrderServiceTest test
```

Run repository integration tests:

```powershell
.\mvnw.cmd -Dtest=OrderRepositoryIntegrationTest test
```

Run one specific test:

```powershell
.\mvnw.cmd -Dtest=OrderControllerTests#searchesOrdersByStatus test
```

Run complete test suite:

```powershell
.\mvnw.cmd clean test
```

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

---

# 34. Manual API Testing

Existing API:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders" `
  -Method GET
```

Search without filters:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search" `
  -Method GET
```

Search by status:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?status=CREATED" `
  -Method GET
```

Search by customer:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?customerId=1" `
  -Method GET
```

Combined search:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?customerId=1&status=CREATED" `
  -Method GET
```

Pagination:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?page=0&size=2" `
  -Method GET
```

Sorting:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?page=0&size=10&sort=id,desc" `
  -Method GET
```

Complete query:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?customerId=1&status=CREATED&page=0&size=10&sort=id,desc" `
  -Method GET
```

Empty search:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/orders/search?customerId=999999999&page=0&size=10" `
  -Method GET
```

---

# 35. Git Workflow

Before committing:

```powershell
git status
```

Review summary:

```powershell
git diff --stat
```

Review actual changes:

```powershell
git diff
```

Check whitespace errors:

```powershell
git diff --check
```

Because the Day 9 ZIP contained pre-existing changes, Day 10 changes were staged selectively instead of blindly using:

```powershell
git add .
```

---

# 36. Day 10 Files Changed

Primary Day 10 implementation files:

```text
src/main/java/com/faris/enterprise_order_api/controller/OrderController.java

src/main/java/com/faris/enterprise_order_api/dto/OrderPageResponse.java

src/main/java/com/faris/enterprise_order_api/repository/OrderRepository.java

src/main/java/com/faris/enterprise_order_api/service/OrderService.java
```

Tests:

```text
src/test/java/com/faris/enterprise_order_api/controller/OrderControllerTests.java

src/test/java/com/faris/enterprise_order_api/repository/OrderRepositoryIntegrationTest.java

src/test/java/com/faris/enterprise_order_api/service/OrderServiceTest.java
```

Documentation:

```text
docs/days/day-10.md
```

---

# 37. Git Commit

Planned Day 10 commit:

```powershell
git commit -m "feat(order): add querying pagination and sorting"
```

Push:

```powershell
git push origin main
```

Final verification:

```powershell
git status
```

Expected:

```text
nothing to commit, working tree clean
```

---

# 38. Key Concepts to Memorize

## Spring Data Derived Query

```text
findByStatus()
```

means:

```text
Find orders where status matches.
```

---

## Nested Property

```text
findByCustomer_Id()
```

means:

```text
Order.customer.id
```

---

## Combined Query

```text
findByCustomer_IdAndStatus()
```

means:

```text
customer.id AND status
```

---

## Pageable

```text
Pageable
 ├── page
 ├── size
 └── sort
```

---

## Page

```text
Page<T>
 ├── content
 ├── totalElements
 ├── totalPages
 ├── number
 ├── size
 ├── first
 └── last
```

---

# 39. Common Mistakes

### Mistake 1 — Loading everything and filtering in Java

```java
repository.findAll()
    .stream()
    .filter(...)
```

For large datasets this is inefficient.

Prefer database filtering.

---

### Mistake 2 — Returning entities directly

Avoid:

```java
return orderRepository.findAll();
```

from the REST controller.

Use DTOs.

---

### Mistake 3 — Putting database logic in controllers

Avoid:

```text
Controller
    ↓
Repository
    ↓
database
```

with business logic inside the controller.

Prefer:

```text
Controller
    ↓
Service
    ↓
Repository
```

---

### Mistake 4 — Using native SQL unnecessarily

Don't write native SQL when a derived query is sufficient.

---

### Mistake 5 — Breaking the API unnecessarily

Changing:

```text
List<OrderResponse>
```

to:

```text
Page<OrderResponse>
```

is a contract change.

Consider backward compatibility.

---

### Mistake 6 — Weak tests

Avoid tests that depend on:

```text
"the database is empty"
```

Tests should create and identify their own data.

---

# 40. Enterprise Lessons

Day 10 introduced several concepts used heavily in real Java backend systems.

### Lesson 1

Database filtering should generally happen in the database.

### Lesson 2

Pagination prevents unnecessarily loading large datasets.

### Lesson 3

Sorting belongs close to the database query.

### Lesson 4

Spring Data JPA can remove a significant amount of repetitive repository code.

### Lesson 5

API compatibility is an architectural concern.

### Lesson 6

DTOs allow the external API contract to remain independent of persistence implementation.

### Lesson 7

Integration tests need deterministic test data.

### Lesson 8

Repository, service, and controller responsibilities should remain separated.

---

# 41. Day 10 Mental Model

Remember:

```text
HTTP
 |
 | status/customerId/page/size/sort
 v
Controller
 |
 | parameters + Pageable
 v
Service
 |
 | choose query
 v
Repository
 |
 | derived query + Pageable
 v
JPA/Hibernate
 |
 v
PostgreSQL
```

And:

```text
WHERE
ORDER BY
LIMIT
OFFSET
```

should happen at the database level whenever appropriate.

---

# 42. Day 10 Final Checklist

* [x] Spring Data JPA derived queries
* [x] Status filtering
* [x] Customer filtering
* [x] Combined filtering
* [x] Nested property query
* [x] `Pageable`
* [x] `Page<T>`
* [x] Pagination
* [x] Sorting
* [x] Custom pagination DTO
* [x] Database-side filtering
* [x] Controller tests
* [x] Service tests
* [x] Repository integration tests
* [x] Empty result handling
* [x] API compatibility considered
* [x] Integration-test isolation issue diagnosed
* [x] Integration-test isolation issue fixed

---

# 43. One-Minute Revision

```text
DAY 10

Spring Data JPA
    ↓
Derived Queries

findByStatus()
findByCustomer_Id()
findByCustomer_IdAndStatus()

Filtering:
status
customerId
combined

Pagination:
Pageable
    page
    size
    sort

Result:
Page<T>

API:
GET /api/v1/orders/search

Architecture:

Controller
    ↓
Service
    ↓
Repository
    ↓
JPA/Hibernate
    ↓
PostgreSQL

Key principle:

Filter + sort + paginate in DB.

Key API principle:

Don't break existing contracts unnecessarily.

Key testing principle:

Don't depend on shared database state.
```

---

# Day 10 Status

**Completed: Spring Data JPA Querying, Filtering, Pagination & Sorting**

The Enterprise Order API now has a proper foundation for more advanced search and data-access features that will be introduced in later days.
