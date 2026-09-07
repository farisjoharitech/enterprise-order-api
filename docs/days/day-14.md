# Day 14 — Database Transactions & Concurrency

**Project:** Enterprise Order API
**Day:** 14 / 365
**Focus:** Database Transactions, ACID, Concurrency, and Optimistic Locking

---

## 1. Day 14 Objective

Today the Enterprise Order API was extended to support and demonstrate:

* Database transactions
* Transaction boundaries
* `@Transactional`
* Read-only transactions
* Commit
* Rollback
* ACID atomicity
* Runtime exception rollback
* Concurrent transactions
* Lost-update protection
* Optimistic locking
* JPA `@Version`
* Flyway database migration
* Transaction/concurrency integration tests

The goal was not only to learn the concepts but to implement and verify them in the existing Enterprise Order API.

---

# 2. Architecture Before Day 14

The application already had the following flow:

```text
HTTP Request
     |
     v
OrderController
     |
     v
OrderService
     |
     v
OrderRepository / CustomerRepository
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

Day 14 strengthens the service/database boundary:

```text
HTTP Request
     |
     v
OrderController
     |
     v
@Transactional OrderService
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

For concurrency:

```text
Transaction A ─────┐
                    ├── PostgreSQL
Transaction B ─────┘
         |
         v
   @Version
         |
         v
Optimistic Locking
```

---

# 3. ACID Transactions

A database transaction is a group of operations treated as one logical unit of work.

ACID means:

```text
A = Atomicity
C = Consistency
I = Isolation
D = Durability
```

## 3.1 Atomicity

Atomicity means:

> All operations in a transaction succeed, or all are rolled back.

Example:

```text
BEGIN
    Operation A
    Operation B
    Operation C
COMMIT
```

If an operation fails:

```text
BEGIN
    Operation A
    Operation B
    ERROR
ROLLBACK
```

The successful operations before the error are also undone.

This is particularly important for banking systems.

For example:

```text
Debit Account A
Credit Account B
```

must behave as one atomic operation.

We do not want:

```text
Debit A       SUCCESS
Credit B      FAILURE
```

Instead:

```text
Debit A
Credit B
    |
    v
COMMIT
```

or:

```text
Debit A
Credit B
    |
    v
ROLLBACK
```

---

# 4. Consistency

Consistency means a transaction moves the database from one valid state to another valid state.

Example:

```text
orders.customer_id
        |
        v
customers.id
```

The foreign-key constraint prevents an order from referencing a customer that does not exist.

Therefore:

```text
Valid state
    |
    | transaction
    v
Valid state
```

The database constraints help enforce consistency.

---

# 5. Isolation

Isolation controls how concurrent transactions interact with each other.

Example:

```text
Transaction A
       |
       | reads order
       v
version = 0

Transaction B
       |
       | reads same order
       v
version = 0
```

Both transactions may initially see the same version.

When one transaction updates the record:

```text
version 0
   |
   v
version 1
```

the other transaction has stale data.

Optimistic locking detects this situation.

---

# 6. Durability

Durability means that once a transaction commits, its changes are persisted.

Conceptually:

```text
Application
     |
     v
COMMIT
     |
     v
Database
     |
     v
Data remains persisted
```

A successful commit means the application can treat the transaction as completed.

---

# 7. `@Transactional`

Spring provides transaction management using:

```java
@Transactional
```

The annotation defines a transaction boundary around a method.

Example:

```java
@Transactional
public OrderResponse createOrder(...) {
    ...
}
```

Conceptually:

```text
Method starts
     |
     v
BEGIN TRANSACTION
     |
     v
execute business logic
     |
     v
COMMIT
```

If a runtime exception occurs:

```text
Method starts
     |
     v
BEGIN TRANSACTION
     |
     v
business operation
     |
     v
RuntimeException
     |
     v
ROLLBACK
```

---

# 8. Transaction Boundaries in `OrderService`

The existing `OrderService` uses transaction boundaries for database operations.

## Read operations

The read methods use:

```java
@Transactional(readOnly = true)
```

Examples:

```java
@Transactional(readOnly = true)
public List<OrderResponse> getAllOrders()
```

```java
@Transactional(readOnly = true)
public List<OrderResponse> getOrdersWithCustomers()
```

```java
@Transactional(readOnly = true)
public OrderPageResponse searchOrders(...)
```

```java
@Transactional(readOnly = true)
public OrderResponse getOrderById(Long id)
```

`readOnly = true` communicates that the transaction is intended for reading.

---

# 9. Write Transactions

Write operations use:

```java
@Transactional
```

Examples:

```java
@Transactional
public OrderResponse createOrder(...)
```

```java
@Transactional
public OrderResponse updateOrder(...)
```

```java
@Transactional
public void deleteOrder(...)
```

The distinction is:

```text
READ
    |
    v
@Transactional(readOnly = true)

WRITE
    |
    v
@Transactional
```

---

# 10. Why Transactions Belong at the Service Layer

The service layer represents business operations.

For example:

```text
Create Order
Update Order
Delete Order
Search Orders
```

Therefore, transaction boundaries are placed around business operations rather than individual repository calls.

Preferred architecture:

```text
Controller
    |
    v
Service
    |
    +---- Transaction Boundary
    |
    v
Repository
```

rather than:

```text
Controller
    |
    v
Repository
    |
    +---- Transaction Boundary
```

The service layer can eventually coordinate multiple repositories within one transaction.

---

# 11. Flyway V3 — Optimistic Locking Column

Day 14 added a new database migration:

```text
src/main/resources/db/migration/V3__add_order_version.sql
```

Migration:

```sql
ALTER TABLE orders
    ADD COLUMN version BIGINT;

UPDATE orders
SET version = 0
WHERE version IS NULL;

ALTER TABLE orders
    ALTER COLUMN version SET DEFAULT 0;

ALTER TABLE orders
    ALTER COLUMN version SET NOT NULL;
```

Migration strategy:

```text
V1
 |
 | Flyway baseline
 v
V2
 |
 | created_at
 v
V3
 |
 | version
 v
Current Schema
```

Existing rows were initialized with:

```text
version = 0
```

The column has:

```text
NOT NULL
DEFAULT 0
```

---

# 12. PostgreSQL Schema After V3

The `orders` table now contains:

```text
id
customer_name
product_name
quantity
status
customer_id
created_at
version
```

The important new column is:

```text
version BIGINT NOT NULL DEFAULT 0
```

The schema was verified directly using PostgreSQL.

Command:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h localhost `
    -p 5433 `
    -U postgres `
    -d enterprise_order_db
```

Then:

```sql
\d orders
```

The database confirmed:

```text
version | bigint | not null | 0
```

---

# 13. JPA Optimistic Locking

The `Order` entity now contains:

```java
import jakarta.persistence.Version;
```

and:

```java
@Version
@Column(nullable = false)
private Long version;
```

The complete concept is:

```text
Order
 |
 +-- id
 +-- customer
 +-- productName
 +-- quantity
 +-- status
 +-- createdAt
 +-- version
```

The `version` field is managed by Hibernate.

The application should NOT manually increment it.

Do not do:

```java
order.setVersion(order.getVersion() + 1);
```

Instead Hibernate manages the version.

---

# 14. Why `@Version` Exists

Without optimistic locking:

```text
Transaction A reads Order
Transaction B reads Order

A updates Order
B updates Order

B may overwrite A
```

This is called a:

```text
Lost Update
```

With optimistic locking:

```text
Transaction A
    |
    | version = 0
    |
    v
UPDATE
    |
    v
version = 1

Transaction B
    |
    | stale version = 0
    |
    v
UPDATE
    |
    v
OptimisticLockException
```

The second transaction cannot silently overwrite the first transaction.

---

# 15. Optimistic Locking Example

Suppose:

```text
Order ID = 100
version = 0
```

Two transactions load the same order.

```text
Transaction A
version = 0

Transaction B
version = 0
```

Transaction A updates:

```text
UPDATE orders
SET status = 'PROCESSING',
    version = 1
WHERE id = 100
AND version = 0;
```

The update succeeds.

Database:

```text
version = 1
```

Transaction B still has:

```text
version = 0
```

It attempts:

```text
UPDATE orders
SET status = 'CANCELLED',
    version = 1
WHERE id = 100
AND version = 0;
```

No row matches because the database now contains:

```text
version = 1
```

Hibernate detects the conflict.

Result:

```text
OptimisticLockException
```

---

# 16. Important Concurrency Lesson

Optimistic locking assumes:

> Conflicts are possible but relatively uncommon.

Instead of locking the database row immediately, the application allows transactions to proceed and checks for a conflict when updating.

Conceptually:

```text
Read
 |
 v
Work
 |
 v
Check version
 |
 +---- unchanged ---> UPDATE
 |
 +---- changed -----> CONFLICT
```

This can provide better concurrency than pessimistically locking every record.

---

# 17. Initial Version Test

A test was created in:

```text
src/test/java/com/faris/enterprise_order_api/service/OrderServiceTransactionTests.java
```

The test creates an order and verifies:

```java
assertNotNull(order.getId());
assertNotNull(order.getVersion());
assertEquals(0L, order.getVersion());
```

This verifies that the newly persisted entity receives the expected initial version.

Test result:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0
```

---

# 18. Concurrent Optimistic Locking Test

A real concurrency test was implemented using:

```java
CountDownLatch
```

and:

```java
TransactionTemplate
```

Two separate threads execute separate transactions.

Conceptually:

```text
Thread A                         Thread B
   |                                |
   | load version 0                 |
   |                                | load version 0
   |                                |
   | update                         |
   | flush                          |
   |                                |
   |                                | update stale entity
   |                                |
   | commit                         |
   |                                |
   |                         OptimisticLockException
```

The test verifies that the stale transaction fails.

The test result:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0
```

---

# 19. Test Data Isolation

The first implementation used fixed customer emails:

```text
version-test@example.com
concurrency@example.com
```

When the tests were run repeatedly against the persistent PostgreSQL database, PostgreSQL correctly rejected duplicate emails.

Example:

```text
duplicate key value violates unique constraint
```

The tests were improved to generate unique email addresses:

```java
"version-test-" + UUID.randomUUID() + "@example.com"
```

and:

```java
"concurrency-" + UUID.randomUUID() + "@example.com"
```

This makes repeated test execution safer.

General lesson:

> Tests should not depend on leftover data from previous executions.

Desired behavior:

```text
Run 1 → PASS
Run 2 → PASS
Run 3 → PASS
Run 100 → PASS
```

---

# 20. Transaction Rollback Test

A rollback test was added.

The test starts a transaction:

```java
TransactionTemplate transactionTemplate =
        new TransactionTemplate(transactionManager);
```

A customer is saved:

```java
Customer customer =
        customerRepository.save(
                new Customer(
                        "Rollback Customer",
                        email
                )
        );
```

The persistence context is flushed:

```java
entityManager.flush();
```

Then a runtime exception is deliberately thrown:

```java
throw new IllegalStateException(
        "Simulated business failure"
);
```

The test expects:

```java
assertThrows(
        IllegalStateException.class,
        ...
);
```

After the transaction fails, the test verifies that the customer does not remain in the database.

Conceptually:

```text
BEGIN
 |
 +-- INSERT customer
 |
 +-- flush()
 |
 +-- IllegalStateException
 |
 v
ROLLBACK
 |
 v
Customer does not exist
```

This demonstrates transaction atomicity.

---

# 21. Why `flush()` Was Used

The test explicitly calls:

```java
entityManager.flush();
```

This forces Hibernate to synchronize the persistence context with the database.

Without the flush, the SQL `INSERT` might remain pending in the persistence context until transaction commit.

For this test we want:

```text
save()
  |
  v
flush()
  |
  v
SQL INSERT
  |
  v
exception
  |
  v
ROLLBACK
```

This makes the rollback behavior explicit.

---

# 22. Runtime Exception and Rollback

The rollback test uses:

```java
IllegalStateException
```

which is an unchecked exception.

The inheritance relationship is:

```text
Throwable
   |
   +-- Exception
        |
        +-- RuntimeException
             |
             +-- IllegalStateException
```

Spring's standard transaction behavior rolls back transactions when an unchecked/runtime exception escapes the transactional operation.

This is an important rule to remember when designing Spring services.

---

# 23. Day 14 Test Suite

The dedicated transaction test class now contains three tests:

```text
OrderServiceTransactionTests
│
├── shouldPersistOrderWithInitialVersion()
│
├── shouldPreventConcurrentUpdatesWithOptimisticLocking()
│
└── shouldRollbackTransactionWhenRuntimeExceptionOccurs()
```

The final dedicated test result:

```text
Tests run: 3
Failures: 0
Errors: 0
Skipped: 0
```

Therefore:

```text
3 / 3 PASS
```

---

# 24. Problems Encountered

## Problem 1 — V3 was initially staged empty

Git showed:

```text
new file: V3__add_order_version.sql
```

but the staged version was empty while the working tree contained the SQL.

Solution:

```powershell
git add src/main/resources/db/migration/V3__add_order_version.sql
```

Lesson:

> Git staging represents a snapshot. If a file is modified after `git add`, the new changes must be staged again.

---

## Problem 2 — Duplicate test data

The tests initially used fixed emails.

PostgreSQL returned:

```text
duplicate key value violates unique constraint
```

Solution:

```java
UUID.randomUUID()
```

was used to generate unique test data.

Lesson:

> Integration tests must be repeatable and should control their test data.

---

## Problem 3 — Repository method did not exist

The rollback test initially attempted:

```java
customerRepository.findByEmail(email)
```

but `CustomerRepository` did not expose that method.

The compiler reported:

```text
cannot find symbol
method findByEmail(String)
```

Solution:

```java
customerRepository.findAll()
```

was used with:

```java
.noneMatch(customer -> email.equals(customer.getEmail()))
```

Lesson:

> Tests should use the application's existing repository API unless a new repository capability is actually required by the domain.

---

# 25. Commands Used

## Run dedicated Day 14 tests

```powershell
.\mvnw.cmd -Dtest=OrderServiceTransactionTests test
```

## Run the optimistic-locking test only

```powershell
.\mvnw.cmd -Dtest=OrderServiceTransactionTests#shouldPreventConcurrentUpdatesWithOptimisticLocking test
```

## Run the rollback test only

```powershell
.\mvnw.cmd -Dtest=OrderServiceTransactionTests#shouldRollbackTransactionWhenRuntimeExceptionOccurs test
```

## Run the complete project test suite

```powershell
.\mvnw.cmd clean test
```

## Check Git

```powershell
git status
```

## Check unstaged changes

```powershell
git diff
```

## Check staged changes

```powershell
git diff --cached
```

## Check whitespace

```powershell
git diff --check
```

## Check staged whitespace

```powershell
git diff --cached --check
```

---

# 26. Database Verification Commands

Connect to PostgreSQL:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h localhost `
    -p 5433 `
    -U postgres `
    -d enterprise_order_db
```

Inspect the orders table:

```sql
\d orders
```

Inspect schema columns:

```sql
SELECT column_name,
       data_type,
       is_nullable,
       column_default
FROM information_schema.columns
WHERE table_name = 'orders'
ORDER BY ordinal_position;
```

Inspect Flyway history:

```sql
SELECT installed_rank,
       version,
       description,
       script,
       success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Expected migration progression:

```text
1 | 1 | << Flyway Baseline >>
2 | 2 | add created at to orders
3 | 3 | add order version
```

Exit PostgreSQL:

```sql
\q
```

---

# 27. Important Enterprise Concepts Learned

Day 14 introduced several concepts that are frequently used in enterprise backend systems:

```text
@Transactional
Transaction Boundary
ACID
Atomicity
Consistency
Isolation
Durability
Commit
Rollback
Runtime Exception
readOnly Transaction
Concurrency
Concurrent Transaction
Lost Update
Optimistic Locking
@Version
OptimisticLockException
EntityManager.flush()
TransactionTemplate
```

---

# 28. Banking Relevance

Transactions and concurrency are critical in banking systems.

Consider:

```text
Account Balance = RM10,000
```

Two transactions attempt to modify the same account.

Without proper concurrency handling:

```text
Transaction A reads RM10,000
Transaction B reads RM10,000

A calculates RM9,000
B calculates RM9,500

A saves RM9,000
B saves RM9,500

Final balance = RM9,500
```

Transaction A's update has effectively been lost.

With proper concurrency control:

```text
Transaction A
version = 0
      |
      v
update
      |
      v
version = 1

Transaction B
version = 0
      |
      v
stale version detected
      |
      v
OptimisticLockException
```

The application can then decide whether to:

```text
retry
reject
notify user
refresh data
```

This is much safer for financial operations.

---

# 29. Current Project Architecture

After Day 14:

```text
Enterprise Order API
│
├── Controller
│   └── OrderController
│
├── Service
│   └── OrderService
│       ├── @Transactional
│       └── @Transactional(readOnly = true)
│
├── Repository
│   ├── OrderRepository
│   └── CustomerRepository
│
├── Specification
│   └── OrderSpecification
│
├── Entity
│   ├── Customer
│   └── Order
│       └── @Version
│
├── Database
│   └── PostgreSQL
│
├── Migration
│   ├── V1__baseline.sql
│   ├── V2__add_created_at_to_orders.sql
│   └── V3__add_order_version.sql
│
└── Tests
    └── OrderServiceTransactionTests
        ├── initial version
        ├── optimistic locking
        └── rollback
```

---

# 30. Day 14 Knowledge Summary

The most important points to remember:

### Transaction

A transaction groups database operations into one logical unit.

### ACID

```text
Atomicity
Consistency
Isolation
Durability
```

### `@Transactional`

Defines a Spring transaction boundary.

### `readOnly = true`

Used for methods intended to perform read operations.

### Rollback

A failed transaction can undo database changes.

### Optimistic locking

Allows concurrent work while detecting conflicting updates.

### `@Version`

JPA's standard mechanism for optimistic locking.

### Lost update

Occurs when one transaction silently overwrites another transaction's changes.

### `flush()`

Synchronizes the persistence context with the database.

### Test isolation

Tests should be repeatable and should not depend on stale data.

---

# 31. Notebook Quick Notes

```text
DAY 14 — TRANSACTIONS & CONCURRENCY

ACID:
A = Atomicity
C = Consistency
I = Isolation
D = Durability

@Transactional:
- Defines transaction boundary
- Runtime exception → rollback by default
- Use readOnly=true for read operations

OrderService:
- create/update/delete → @Transactional
- get/search → @Transactional(readOnly=true)

Optimistic Locking:
- Add @Version to entity
- Hibernate manages version
- Never manually increment version
- Prevents lost updates

Database:
V3__add_order_version.sql
version BIGINT NOT NULL DEFAULT 0

Flow:
Transaction A reads version 0
Transaction B reads version 0
A updates → version 1
B updates stale version 0
→ OptimisticLockException

Rollback:
BEGIN
INSERT
flush
RuntimeException
ROLLBACK
INSERT does not remain

Tests:
1. Initial version
2. Concurrent update conflict
3. Runtime exception rollback

Result:
3 tests
0 failures
0 errors
0 skipped
```

---

# 32. Day 14 Completion Criteria

| Requirement                | Status   |
| -------------------------- | -------- |
| Understand ACID            | Complete |
| Understand transactions    | Complete |
| `@Transactional`           | Complete |
| Read-only transactions     | Complete |
| Commit                     | Complete |
| Rollback                   | Complete |
| Runtime exception rollback | Complete |
| Concurrency                | Complete |
| Lost updates               | Complete |
| Optimistic locking         | Complete |
| JPA `@Version`             | Complete |
| Flyway V3                  | Complete |
| Initial version test       | Complete |
| Concurrent locking test    | Complete |
| Rollback test              | Complete |
| Test data isolation        | Complete |
| Documentation              | Complete |

---

# 33. Git Checkpoint

Before committing:

```powershell
git status
```

Then:

```powershell
git diff --check
```

Stage everything belonging to Day 14:

```powershell
git add .
```

Verify staged changes:

```powershell
git diff --cached --check
```

Review:

```powershell
git diff --cached
```

Commit:

```powershell
git commit -m "feat(order): add transaction and optimistic locking"
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
On branch main
Your branch is up to date with 'origin/main'.

nothing to commit, working tree clean
```

---

# 34. Day 14 Final Outcome

The Enterprise Order API now has:

```text
Database migrations
        +
Transaction boundaries
        +
Rollback behavior
        +
Concurrent transaction handling
        +
Optimistic locking
        +
Automated tests
```

The project has moved from simply **persisting data** to protecting data integrity when multiple operations occur concurrently.

This is an important foundation for the future banking-oriented parts of the project, where transaction correctness and concurrency control become critical.
