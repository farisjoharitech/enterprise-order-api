# Day 7 — Transactions & Service Boundaries

## Objective

Introduce Spring transaction management into the Order Service.

## Main Concept

A transaction groups database operations into one logical unit of work.

```text
BEGIN
  ↓
Business Operation
  ↓
Success → COMMIT
Failure → ROLLBACK
```

## ACID

### Atomicity

All operations succeed or all operations roll back.

### Consistency

Database rules remain valid.

### Isolation

Concurrent transactions are isolated according to the configured isolation level.

### Durability

Committed data survives application restart.

## Spring Transaction

```java
@Transactional
```

Used for database write operations.

```java
@Transactional(readOnly = true)
```

Used for database read operations.

## Transaction Boundary

Transactions are placed at the Service layer.

```text
Controller
    ↓
Service
    ↓
@Transactional
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
PostgreSQL
```

## Why Service Layer?

A service represents a business operation.

Example:

```text
Create Order
    ↓
Validate Customer
    ↓
Create Order
    ↓
Commit
```

The entire operation should have one transaction boundary.

## JPA Persistence Context

Inside a transaction Hibernate manages entities.

```text
find()
  ↓
Managed Entity
  ↓
Change Entity
  ↓
Dirty Checking
  ↓
SQL UPDATE
  ↓
COMMIT
```

## Dirty Checking

Hibernate detects changes to managed entities and synchronizes those changes with the database during transaction completion.

Example:

```java
@Transactional
public void updateOrder(...) {
    Order order = repository.findById(id)
        .orElseThrow(...);

    order.setQuantity(5);
}
```

Hibernate can detect the changed quantity.

## Important Rule

Do not remove existing `save()` calls just to demonstrate dirty checking.

Understand the concept first.

Avoid unnecessary refactoring.

## Read Transactions

Read operations use:

```java
@Transactional(readOnly = true)
```

This communicates that the operation is intended for reading.

## Day 7 Implementation

Updated the existing Order Service with transaction boundaries.

### Writes

* Create Order
* Update Order
* Delete Order

Use:

```java
@Transactional
```

### Reads

* Get Order
* Get All Orders

Use:

```java
@Transactional(readOnly = true)
```

## Testing

Verified:

* GET orders
* GET order by ID
* POST order
* PUT order
* DELETE order
* validation
* missing order handling
* PostgreSQL persistence

## Rollback

A realistic rollback test was not artificially added.

A meaningful multi-step transactional operation will be introduced later when the project has multiple business operations such as payment, account balance, inventory, or audit processing.

## Key Lessons

1. Transactions protect business operations.
2. `@Transactional` defines a transaction boundary.
3. Service methods are a natural transaction boundary.
4. ACID describes fundamental transaction guarantees.
5. Hibernate uses a persistence context to manage entities.
6. Dirty checking detects changes to managed entities.
7. `readOnly=true` communicates read intent.
8. Don't add artificial complexity only for demonstration.
9. Don't refactor unrelated code while learning transactions.
10. Database integrity and transaction management work together.

## Architecture

```text
Client
  ↓
REST Controller
  ↓
Order Service
  ↓
@Transactional
  ↓
Spring Data Repository
  ↓
JPA
  ↓
Hibernate
  ↓
PostgreSQL
```

## Day 7 Definition of Done

* [ ] OrderService inspected
* [ ] Transaction boundaries identified
* [ ] Write methods transactional
* [ ] Read methods read-only transactional
* [ ] Existing business logic preserved
* [ ] Compilation successful
* [ ] Existing tests pass
* [ ] REST API verified
* [ ] PostgreSQL persistence verified
* [ ] Clean tests pass
* [ ] Git diff reviewed
* [ ] Day 7 documentation added
* [ ] Commit created
* [ ] GitHub pushed
