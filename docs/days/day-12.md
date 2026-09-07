# Day 12 — Dynamic Filtering, Customer CRUD & Exception Handling

**Project:** Enterprise Order API
**Day:** 12
**Focus:** Spring Data JPA Specifications, Dynamic Filtering, Customer CRUD, REST Exception Handling
**Stack:** Java 17, Spring Boot 4.1.1, Spring Data JPA, Hibernate, PostgreSQL 17, Maven, Postman

---

## 1. Day 12 Objective

Extend the existing Enterprise Order API with:

1. Dynamic order filtering using Spring Data JPA Specifications.
2. Pagination and sorting together with dynamic filters.
3. Customer CRUD API.
4. Centralized customer/order exception handling.
5. Consistent REST error responses.
6. Manual API verification using Postman.

The implementation continues the same evolving project.

No separate practice application was created.

---

# 2. Topics Learned

## Spring Data JPA Specifications

Specifications allow queries to be constructed dynamically at runtime.

Instead of creating a repository method for every possible combination:

```text
findByStatus(...)
findByCustomerId(...)
findByStatusAndCustomerId(...)
findByStatusAndProductName(...)
findByStatusAndQuantity(...)
findByCustomerIdAndProductName(...)
...
```

we can compose independent filtering rules.

Example:

```java
Specification<Order> specification =
        Specification
                .where(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.hasCustomerId(customerId))
                .and(OrderSpecification.productNameContains(productName))
                .and(OrderSpecification.quantityGreaterThanOrEqualTo(minimumQuantity))
                .and(OrderSpecification.quantityLessThanOrEqualTo(maximumQuantity));
```

This allows any combination of filters.

---

# 3. Repository Changes

`OrderRepository` now extends:

```java
JpaSpecificationExecutor<Order>
```

The repository structure is:

```java
public interface OrderRepository
        extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {
}
```

`JpaRepository` provides normal CRUD functionality.

`JpaSpecificationExecutor` provides dynamic Specification-based queries.

The service can therefore execute:

```java
orderRepository.findAll(specification, pageable);
```

---

# 4. OrderSpecification

Created:

```text
src/main/java/com/faris/enterprise_order_api/specification/OrderSpecification.java
```

The class contains reusable filtering rules.

## Status

```java
public static Specification<Order> hasStatus(String status)
```

Behavior:

```text
status is null/blank
        ↓
no predicate

status supplied
        ↓
status = requested status
```

---

## Customer ID

```java
public static Specification<Order> hasCustomerId(Long customerId)
```

Filters through the relationship:

```text
Order
  ↓
customer
  ↓
id
```

Conceptually:

```sql
customer_id = ?
```

---

## Product Name

```java
public static Specification<Order> productNameContains(String productName)
```

Uses a case-insensitive search.

Conceptually:

```sql
LOWER(product_name) LIKE '%laptop%'
```

Therefore:

```text
Laptop
laptop
LAPTOP
LaPtOp
```

can match the same products.

---

## Minimum Quantity

```java
public static Specification<Order> quantityGreaterThanOrEqualTo(
        Integer minimumQuantity
)
```

Equivalent logic:

```text
quantity >= minimumQuantity
```

---

## Maximum Quantity

```java
public static Specification<Order> quantityLessThanOrEqualTo(
        Integer maximumQuantity
)
```

Equivalent logic:

```text
quantity <= maximumQuantity
```

---

# 5. Dynamic Filter Composition

The service combines the individual Specifications using:

```java
.and(...)
```

Example:

```text
status
   AND
customerId
   AND
productName
   AND
minimumQuantity
   AND
maximumQuantity
```

Any filter can be omitted.

For example:

```text
/search?status=CREATED
```

only applies the status Specification.

While:

```text
/search?status=CREATED&productName=Laptop&minimumQuantity=2&maximumQuantity=10
```

applies all four filters.

---

# 6. Pagination

Dynamic filtering still supports Spring Data pagination.

The service receives:

```java
Pageable pageable
```

and executes:

```java
orderRepository.findAll(specification, pageable);
```

The API returns:

```text
content
page
size
totalElements
totalPages
first
last
```

This allows the client to understand both:

* the current page
* the overall result set

---

# 7. Sorting

The existing controller continues to use:

```java
@PageableDefault(
        size = 20,
        sort = "id",
        direction = Sort.Direction.DESC
)
```

Clients can override sorting.

Example:

```text
?sort=quantity,asc
```

or:

```text
?sort=quantity,desc
```

---

# 8. Day 12 Search Endpoint

The endpoint is:

```http
GET /api/v1/orders/search
```

Supported parameters:

```text
status
customerId
productName
minimumQuantity
maximumQuantity
page
size
sort
```

Examples:

```http
GET /api/v1/orders/search
```

```http
GET /api/v1/orders/search?status=CREATED
```

```http
GET /api/v1/orders/search?customerId=1
```

```http
GET /api/v1/orders/search?productName=Laptop
```

```http
GET /api/v1/orders/search?minimumQuantity=5
```

```http
GET /api/v1/orders/search?maximumQuantity=10
```

```http
GET /api/v1/orders/search?minimumQuantity=2&maximumQuantity=10
```

Multiple filters:

```http
GET /api/v1/orders/search?status=CREATED&productName=Laptop&minimumQuantity=2&maximumQuantity=10
```

Pagination:

```http
GET /api/v1/orders/search?page=0&size=5
```

Sorting:

```http
GET /api/v1/orders/search?sort=quantity,desc
```

Combined:

```http
GET /api/v1/orders/search?status=CREATED&minimumQuantity=2&maximumQuantity=10&page=0&size=5&sort=quantity,desc
```

---

# 9. Customer CRUD

Customer management was added to the same application.

API:

```text
POST   /api/v1/customers
GET    /api/v1/customers
GET    /api/v1/customers/{id}
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}
```

Architecture:

```text
CustomerController
       ↓
CustomerService
       ↓
CustomerRepository
       ↓
PostgreSQL
```

---

# 10. Customer DTOs

Created:

```text
CreateCustomerRequest
UpdateCustomerRequest
CustomerResponse
```

The API does not expose the entity directly.

This maintains the separation:

```text
HTTP API
   ↓
DTO
   ↓
Service
   ↓
Entity
   ↓
Repository
```

---

# 11. Create Customer

Endpoint:

```http
POST /api/v1/customers
```

Request:

```json
{
  "name": "Postman Customer",
  "email": "postman-customer@example.com"
}
```

Expected:

```text
201 Created
```

The response contains:

```json
{
  "id": 1,
  "name": "Postman Customer",
  "email": "postman-customer@example.com"
}
```

The actual ID is database-generated and must not be assumed to be `1`.

---

# 12. Get All Customers

Endpoint:

```http
GET /api/v1/customers
```

Expected:

```text
200 OK
```

Response:

```json
[
  {
    "id": 1,
    "name": "Postman Customer",
    "email": "postman-customer@example.com"
  }
]
```

---

# 13. Get Customer By ID

Endpoint:

```http
GET /api/v1/customers/{id}
```

Example:

```http
GET /api/v1/customers/1
```

Expected:

```text
200 OK
```

---

# 14. Update Customer

Endpoint:

```http
PUT /api/v1/customers/{id}
```

Example:

```json
{
  "name": "Updated Customer",
  "email": "updated@example.com"
}
```

Expected:

```text
200 OK
```

---

# 15. Delete Customer

Endpoint:

```http
DELETE /api/v1/customers/{id}
```

Expected:

```text
204 No Content
```

After deletion:

```http
GET /api/v1/customers/{id}
```

should return:

```text
404 Not Found
```

---

# 16. Customer Validation

Customer creation/update uses Jakarta Bean Validation.

Examples:

```java
@NotBlank
@Email
@Size
```

Invalid requests should be rejected.

Example:

```json
{
  "name": "",
  "email": "invalid-email"
}
```

Expected:

```text
400 Bad Request
```

---

# 17. Customer → Order Relationship

The existing relationship remains:

```text
Customer
    1
    │
    │
    │ *
    ▼
Order
```

Database:

```text
customers
    │
    │ customers.id
    │
    ▼
orders.customer_id
```

An order cannot reference a nonexistent customer.

Order creation therefore performs:

```text
customerId
    ↓
CustomerRepository.findById()
    ↓
Customer exists?
    │
    ├── YES → create Order
    │
    └── NO → CustomerNotFoundException
```

This protects referential integrity at the application level.

---

# 18. Exception Handling

The application already has:

```text
GlobalExceptionHandler
```

located under:

```text
src/main/java/com/faris/enterprise_order_api/exception
```

It uses:

```java
@RestControllerAdvice
```

to provide centralized exception handling.

Existing handlers include:

```text
MethodArgumentNotValidException
HttpMessageNotReadableException
MethodArgumentTypeMismatchException
OrderNotFoundException
ResponseStatusException
```

Day 12 added:

```text
CustomerNotFoundException
```

---

# 19. CustomerNotFoundException

Created:

```text
CustomerNotFoundException
```

The service throws it when a requested customer doesn't exist.

Example:

```java
private Customer findCustomer(Long id) {
    return customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id));
}
```

This is preferable to returning `null`.

---

# 20. REST Error Response

The existing application uses:

```text
dto/error/ErrorResponse
dto/error/FieldErrorResponse
```

The global handler converts application exceptions into consistent HTTP responses.

Example:

```http
GET /api/v1/customers/999999
```

Response:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer 999999 was not found",
  "path": "/api/v1/customers/999999",
  "fieldErrors": []
}
```

The exact timestamp/fields depend on the current `ErrorResponse` implementation.

---

# 21. Why Global Exception Handling Is Important

Without centralized handling:

```text
Controller
    ↓
Service
    ↓
Exception
    ↓
Default Spring error handling
```

With centralized handling:

```text
Controller
    ↓
Service
    ↓
Domain/Application Exception
    ↓
GlobalExceptionHandler
    ↓
Consistent REST ErrorResponse
```

This keeps HTTP concerns out of most service logic.

---

# 22. Postman Testing

Customer CRUD was manually tested using Postman.

## CRUD

```text
[ ] POST /api/v1/customers
[ ] GET /api/v1/customers
[ ] GET /api/v1/customers/{id}
[ ] PUT /api/v1/customers/{id}
[ ] GET /api/v1/customers/{id}
[ ] DELETE /api/v1/customers/{id}
[ ] GET deleted customer → 404
```

## Validation

```text
[ ] Blank customer name → 400
[ ] Invalid email → 400
[ ] Missing name → 400
[ ] Missing email → 400
```

## Exception handling

```text
[ ] GET nonexistent customer → 404
[ ] GET nonexistent order → 404
```

## Order integration

```text
[ ] Create customer
[ ] Create order using customer ID
[ ] Retrieve order
[ ] Verify customer information
```

## Dynamic filtering

```text
[ ] Search without filters
[ ] Status filter
[ ] Customer ID filter
[ ] Product name filter
[ ] Case-insensitive product search
[ ] Minimum quantity
[ ] Maximum quantity
[ ] Quantity range
[ ] Multiple filters
[ ] Pagination
[ ] Sorting
[ ] Filter + pagination + sorting
[ ] Empty result
```

---

# 23. Environment Issue Discovered

This PC uses:

```text
PostgreSQL 17
```

and PostgreSQL is configured to listen on:

```text
localhost:5433
```

not:

```text
localhost:5432
```

The original configuration pointed to:

```text
jdbc:postgresql://localhost:5432/enterprise_order_db
```

which caused the application to fail to establish a database connection.

The PostgreSQL service was running, but nothing was accepting connections on port `5432`.

The actual listening port was identified with:

```powershell
netstat -ano | findstr LISTENING | findstr ":543"
```

Result:

```text
TCP    0.0.0.0:5433    0.0.0.0:0    LISTENING
TCP    [::]:5433       [::]:0       LISTENING
```

The datasource was therefore changed to:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/enterprise_order_db
```

The database `enterprise_order_db` was also created on this development machine.

---

# 24. Important Environment Lesson

A Spring Boot application depends on more than Java source code.

The runtime chain is:

```text
Java Application
       ↓
Spring Boot
       ↓
DataSource
       ↓
JDBC
       ↓
Host + Port
       ↓
PostgreSQL
       ↓
Database
       ↓
Schema
```

A failure at any layer can prevent Hibernate from initializing.

This was demonstrated by the error:

```text
Unable to determine Dialect without JDBC metadata
```

The underlying issue was database connectivity rather than a missing Hibernate dialect configuration.

---

# 25. Files Added/Modified

Day 12 introduced or modified functionality around:

```text
src/main/java/com/faris/enterprise_order_api/

dto/
├── CreateCustomerRequest.java
├── UpdateCustomerRequest.java
├── CustomerResponse.java
└── error/
    ├── ErrorResponse.java
    └── FieldErrorResponse.java

exception/
├── CustomerNotFoundException.java
├── OrderNotFoundException.java
└── GlobalExceptionHandler.java

model/
└── Customer.java

repository/
├── CustomerRepository.java
└── OrderRepository.java

service/
├── CustomerService.java
└── OrderService.java

controller/
├── CustomerController.java
└── OrderController.java

specification/
└── OrderSpecification.java
```

Exact files changed may vary depending on the final working tree.

---

# 26. Testing Commands

Run the complete Maven test suite:

```powershell
.\mvnw.cmd test
```

Check whitespace/errors:

```powershell
git diff --check
```

Check repository state:

```powershell
git status
```

---

# 27. Git Workflow

End-of-day workflow:

```powershell
git status
```

Review changes:

```powershell
git diff
```

Check whitespace:

```powershell
git diff --check
```

Stage:

```powershell
git add .
```

Commit:

```powershell
git commit -m "feat(order): add dynamic order filtering"
```

Push:

```powershell
git push origin main
```

---

# 28. Day 12 Architecture

The application now has:

```text
                    REST API
                       │
          ┌────────────┴────────────┐
          │                         │
          ▼                         ▼
 CustomerController          OrderController
          │                         │
          ▼                         ▼
 CustomerService              OrderService
          │                         │
          ▼                         ▼
 CustomerRepository            OrderRepository
                                  │
                                  │
                         JpaSpecificationExecutor
                                  │
                                  ▼
                         OrderSpecification
                                  │
                                  ▼
                              PostgreSQL
```

Exception flow:

```text
Controller
    ↓
Service
    ↓
Exception
    ↓
GlobalExceptionHandler
    ↓
ErrorResponse
    ↓
HTTP response
```

---

# 29. Key Takeaways

### Specification

Specifications allow dynamic database queries to be assembled from reusable predicates.

### JpaSpecificationExecutor

Provides the repository capability to execute Specifications.

### Pagination

`Pageable` allows filtering, pagination and sorting to work together.

### DTO

Customer APIs use request/response DTOs rather than exposing entities directly.

### Service Layer

Business/application logic remains in the service layer.

### Global Exception Handling

`@RestControllerAdvice` centralizes REST error translation.

### Database Configuration

A running PostgreSQL service does not guarantee that the application is using the correct:

```text
host
port
database
username
password
```

### Project-Based Learning

Customer CRUD was implemented inside the existing Enterprise Order API rather than creating a separate application.

---

# 30. Day 12 Definition of Done

```text
[✓] Spring Data JPA Specifications implemented
[✓] Dynamic status filtering
[✓] Dynamic customer filtering
[✓] Dynamic product-name filtering
[✓] Minimum quantity filtering
[✓] Maximum quantity filtering
[✓] Multiple filter composition
[✓] Pagination
[✓] Sorting
[✓] Customer CRUD
[✓] Customer DTOs
[✓] Customer service
[✓] Customer controller
[✓] Customer not-found exception
[✓] Global customer exception handling
[✓] REST error response
[✓] Postman CRUD testing
[✓] Postman exception testing
[✓] Order/customer integration verified
[ ] Automated tests confirmed after final changes
[ ] git diff --check
[ ] Git commit
[ ] Git push
```

---

# Notebook Summary

**Day 12 — Dynamic Filtering + Customer CRUD**

```text
Learned:
- Spring Data JPA Specification
- JpaSpecificationExecutor
- Dynamic query composition
- Predicate composition with AND
- Pagination with Specification
- Sorting with Pageable
- Customer CRUD
- DTO-based API design
- Global exception handling
- @RestControllerAdvice
- Consistent REST error responses
- PostgreSQL host/port troubleshooting

Implemented:
- OrderSpecification
- Dynamic /orders/search
- CustomerController
- CustomerService
- CustomerRepository
- Customer DTOs
- CustomerNotFoundException
- Global Customer exception handler
- Postman CRUD verification

Architecture:
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL

Dynamic search:
Controller
    ↓
OrderService
    ↓
OrderSpecification
    ↓
JpaSpecificationExecutor
    ↓
PostgreSQL

Exception:
Service
    ↓
Exception
    ↓
GlobalExceptionHandler
    ↓
ErrorResponse
    ↓
HTTP 4xx/5xx

Key lesson:
Don't create a repository method for every possible filter combination.
Use Specifications to dynamically compose reusable query predicates.
```
