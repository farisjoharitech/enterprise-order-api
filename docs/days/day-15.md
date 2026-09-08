# Day 15 — Database Indexing & Query Performance

**Project:** `enterprise-order-api`
**Track:** Java Enterprise Backend / Banking Backend Mastery
**Day:** 15 / 365
**Focus:** Database indexes, B-tree indexes, composite indexes, query planning, `EXPLAIN`, `EXPLAIN ANALYZE`, selectivity, pagination performance, index ordering, index trade-offs, and evidence-based optimization

---

# 1. Day 15 Objective

Day 15 moves from:

```text
"Does the query work?"
```

to:

```text
"Does the query work efficiently?"
```

The project already supports:

* Spring Data JPA
* filtering
* dynamic Specifications
* pagination
* sorting
* customer/order relationships
* Flyway migrations
* transactions
* optimistic locking

Today we measure how PostgreSQL executes those queries.

The key principle is:

> **Measure First. Optimize Second.**

Do not add indexes simply because an index sounds useful.

---

# 2. Project Evolution

The relevant progression is:

```text
Day 10
JPA querying
Filtering
Pagination
Sorting
        ↓
Day 11
Fetching
N+1
JOIN FETCH
        ↓
Day 12
Dynamic Specifications
        ↓
Day 13
Flyway
Database evolution
        ↓
Day 14
Transactions
Concurrency
Optimistic locking
        ↓
Day 15
Indexing
Query performance
Execution plans
```

This is the correct progression.

---

# 3. What Is a Database Index?

An index is an additional data structure that helps the database locate rows efficiently.

Without a useful index, PostgreSQL may need to scan many rows.

Conceptually:

```text
Without useful index

Query
 ↓
Scan table
 ↓
Check row
 ↓
Check row
 ↓
Check row
 ↓
...
```

With an appropriate index:

```text
Query
 ↓
Index
 ↓
Locate matching rows
 ↓
Fetch required data
```

An index is therefore an optimization structure.

---

# 4. Important Warning

An index is not automatically good.

Every index has costs:

```text
Index
├── Storage
├── Memory/cache usage
├── INSERT overhead
├── UPDATE overhead
└── DELETE overhead
```

Therefore:

```text
More indexes ≠ automatically better performance
```

---

# 5. Current Order Query Patterns

The project already supports queries involving:

```text
status
customerId
customerId + status
pagination
sorting
```

For example:

```text
GET /api/v1/orders/search
```

can use:

```text
status
customerId
page
size
sort
```

This makes the Order table an excellent candidate for measuring index effectiveness.

---

# 6. Existing Database Index

The current database already has an index on:

```text
orders.customer_id
```

Conceptually:

```sql
CREATE INDEX idx_orders_customer_id
ON orders(customer_id);
```

This makes sense because the application frequently queries orders by customer.

Do not create a duplicate index.

---

# 7. B-Tree Index

PostgreSQL's default index type is generally the B-tree.

It is suitable for many common operations involving:

```text
=
<
>
<=
>=
BETWEEN
ORDER BY
```

For example:

```sql
WHERE customer_id = 10
```

is a classic B-tree use case.

---

# 8. Selectivity

Selectivity describes how effectively a condition narrows the result set.

Suppose:

```text
1,000,000 orders
```

and:

```text
customer_id = 123
```

returns:

```text
20 orders
```

That condition is highly selective.

But:

```text
status = 'PENDING'
```

might return:

```text
400,000 orders
```

That condition is much less selective.

An index may be much more useful for the first query.

---

# 9. Cardinality

Cardinality refers to the number of distinct values.

Example:

```text
customer_id

1
2
3
...
100,000
```

High cardinality.

Whereas:

```text
status

PENDING
COMPLETED
CANCELLED
```

has very low cardinality.

This does **not** mean low-cardinality columns can never benefit from indexes.

The query planner considers the complete situation.

---

# 10. Query Planner

PostgreSQL has a query planner.

Conceptually:

```text
SQL
 ↓
Parser
 ↓
Planner / Optimizer
 ↓
Execution Plan
 ↓
Executor
 ↓
Result
```

The planner evaluates possible strategies.

For example:

```text
Sequential Scan
Index Scan
Bitmap Heap Scan
Index Only Scan
Nested Loop
Hash Join
Merge Join
```

and chooses a plan based on statistics and estimated cost.

---

# 11. `EXPLAIN`

Use:

```sql
EXPLAIN
SELECT *
FROM orders
WHERE customer_id = 1;
```

This shows the planned execution strategy without actually executing the query.

Example conceptual result:

```text
Index Scan using idx_orders_customer_id
```

or:

```text
Seq Scan on orders
```

---

# 12. `EXPLAIN ANALYZE`

Use:

```sql
EXPLAIN (ANALYZE)
SELECT *
FROM orders
WHERE customer_id = 1;
```

Unlike plain `EXPLAIN`, `ANALYZE` executes the query and reports actual execution information.

Useful information includes:

```text
Planning Time
Execution Time
Actual Rows
Estimated Rows
Scan Type
```

---

# 13. `BUFFERS`

For deeper analysis:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1;
```

`BUFFERS` provides information about PostgreSQL buffer activity.

This helps determine whether the query is doing significant I/O work.

---

# 14. Important Rule

Do not look only at:

```text
Execution Time
```

Also examine:

```text
estimated rows
actual rows
scan type
loops
buffers
planning time
```

A query plan is a story about how PostgreSQL executed the request.

---

# 15. Baseline Before Optimization

Before adding an index, establish a baseline.

This is critical.

The optimization workflow is:

```text
Measure
 ↓
Understand
 ↓
Change
 ↓
Measure Again
 ↓
Compare
```

Not:

```text
Guess
 ↓
Add index
 ↓
Hope
```

---

# 16. Windows — Start Day 15

Open PowerShell.

Navigate to the project:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Check location:

```powershell
Get-Location
```

Expected:

```text
C:\Users\User\Documents\JavaProjects\enterprise-order-api
```

---

# 17. Check Git

Run:

```powershell
git status
```

Then:

```powershell
git log --oneline -10
```

Confirm the previous work is committed.

Do not begin database optimization with unrelated uncommitted changes.

---

# 18. Run Existing Tests First

Run:

```powershell
.\mvnw.cmd clean test
```

Before making changes, establish:

```text
Failures = 0
Errors = 0
```

If the baseline is already broken, fix or understand the baseline before claiming a performance improvement.

---

# 19. Connect to PostgreSQL

The project's local PostgreSQL environment uses:

```text
Host: localhost
Port: 5433
Database: enterprise_order_db
User: postgres
```

Run:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
    -h localhost `
    -p 5433 `
    -U postgres `
    -d enterprise_order_db
```

Enter the password when prompted.

Never commit the password to Git.

---

# 20. List Tables

Inside `psql`:

```sql
\dt
```

Confirm:

```text
customers
orders
```

---

# 21. Inspect Orders

Run:

```sql
\d orders
```

Pay attention to:

```text
Columns
Constraints
Indexes
Foreign keys
```

The evolving `orders` table contains fields such as:

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

---

# 22. List Existing Indexes

Run:

```sql
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'orders'
ORDER BY indexname;
```

Do this before creating anything.

The project already has:

```text
idx_orders_customer_id
```

so do not recreate it.

---

# 23. Inspect Table Size

Run:

```sql
SELECT
    pg_size_pretty(pg_relation_size('orders')) AS table_size;
```

Then:

```sql
SELECT
    pg_size_pretty(pg_indexes_size('orders')) AS indexes_size;
```

This introduces another important performance concept:

> Indexes consume physical storage.

---

# 24. Baseline Query 1 — Customer

Run:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1;
```

Record:

```text
Scan type
Execution Time
Actual Rows
Buffers
```

Do not assume the result.

Read what PostgreSQL actually reports.

---

# 25. Baseline Query 2 — Status

Run:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE status = 'PENDING';
```

Observe whether PostgreSQL uses:

```text
Index Scan
```

or:

```text
Seq Scan
```

Do not assume a status index is required.

---

# 26. Baseline Query 3 — Customer + Status

Run:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1
  AND status = 'PENDING';
```

This is particularly important because the application supports combined filtering.

---

# 27. Baseline Query 4 — Filtering + Sorting + Pagination

Run:

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

This represents a realistic API access pattern.

---

# 28. Why Test Real Queries?

Do not optimize imaginary queries.

The application actually uses:

```text
customerId
status
pagination
sorting
```

Therefore database performance analysis should use realistic access patterns.

This is:

> **Measure First.**

---

# 29. Inspect Data Distribution

Run:

```sql
SELECT
    status,
    COUNT(*) AS total
FROM orders
GROUP BY status
ORDER BY total DESC;
```

This tells us how data is distributed by status.

---

# 30. Inspect Customer Distribution

Run:

```sql
SELECT
    customer_id,
    COUNT(*) AS total_orders
FROM orders
GROUP BY customer_id
ORDER BY total_orders DESC
LIMIT 10;
```

This tells us whether some customers have significantly more orders than others.

---

# 31. Update PostgreSQL Statistics

Run:

```sql
ANALYZE orders;
```

This updates planner statistics.

Then repeat an important query:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1
  AND status = 'PENDING';
```

The planner's estimates depend on statistics.

---

# 32. Composite Index

A composite index contains multiple columns.

Candidate:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

This creates an index ordered conceptually by:

```text
customer_id
    ↓
status
```

But do not create it merely because it looks logical.

First understand the workload and baseline plan.

---

# 33. Why Composite Index?

The application frequently supports:

```text
customerId
+
status
```

Therefore:

```sql
WHERE customer_id = ?
  AND status = ?
```

may benefit from:

```text
(customer_id, status)
```

But the query planner decides whether using the index is actually cheaper.

---

# 34. Column Order Matters

These are not identical:

```text
(customer_id, status)
```

and:

```text
(status, customer_id)
```

Index column order matters.

For example:

```text
(customer_id, status)
```

is naturally aligned with queries beginning with:

```text
customer_id
```

The leftmost-prefix behavior of B-tree indexes is an important concept.

---

# 35. Leftmost Prefix

For:

```text
(customer_id, status)
```

the index can generally be useful for queries involving:

```text
customer_id
```

and:

```text
customer_id + status
```

But it is not equivalent to having a dedicated:

```text
(status)
```

index for status-only access.

Therefore:

> **Composite index design must be based on actual query patterns.**

---

# 36. Create the Candidate Index

Only after understanding the baseline should you consider:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

Then run:

```sql
ANALYZE orders;
```

---

# 37. Re-run the Query

Run:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1
  AND status = 'PENDING';
```

Compare before and after.

Record:

```text
Before
──────
Plan:
Execution Time:
Buffers:

After
─────
Plan:
Execution Time:
Buffers:
```

---

# 38. Re-run Pagination Query

Run:

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

Compare again.

---

# 39. Important: An Index May Not Be Used

Do not panic if PostgreSQL still chooses:

```text
Seq Scan
```

The planner may correctly determine that a sequential scan is cheaper.

Reasons can include:

```text
Small table
Low selectivity
Cached data
Query returns many rows
Index overhead
Statistics
Cost estimates
```

Therefore:

> **Index exists ≠ index must be used.**

---

# 40. Small Tables

For a small table:

```text
100 rows
```

a sequential scan may be faster than navigating an index.

The database can simply read the table.

This is why premature indexing is dangerous.

---

# 41. Selectivity Example

Suppose:

```text
1,000,000 rows
```

and:

```text
status = 'PENDING'
```

returns:

```text
900,000 rows
```

An index may not provide much benefit.

But:

```text
customer_id = 12345
```

returns:

```text
20 rows
```

An index is much more attractive.

---

# 42. Indexing `status`

Do not automatically create:

```sql
CREATE INDEX idx_orders_status
ON orders(status);
```

just because the application filters by status.

Measure first.

If the column has only a few values and a large percentage of rows match, a sequential scan may be perfectly reasonable.

---

# 43. Indexing `product_name`

Do not automatically create:

```sql
CREATE INDEX idx_orders_product_name
ON orders(product_name);
```

for:

```sql
WHERE product_name LIKE '%Laptop%';
```

A standard B-tree index generally does not solve arbitrary leading-wildcard searches effectively.

Later, specialized indexing strategies may be considered if real requirements justify them.

---

# 44. Indexing `created_at`

The project has:

```text
created_at
```

but do not automatically add an index.

A future query such as:

```sql
WHERE created_at >= ?
ORDER BY created_at DESC
```

might justify one.

But:

```text
Possible query
```

is not sufficient evidence.

---

# 45. Pagination and OFFSET

Current API pagination uses:

```text
page
size
```

which commonly maps to:

```sql
LIMIT
OFFSET
```

Example:

```sql
LIMIT 20 OFFSET 10000;
```

Large offsets can become expensive.

The database may still need to process/skip many rows before returning the requested page.

---

# 46. OFFSET Example

Compare:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
ORDER BY id
LIMIT 20 OFFSET 0;
```

with:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
ORDER BY id
LIMIT 20 OFFSET 10000;
```

Observe the difference.

---

# 47. Keyset Pagination Preview

A future alternative is keyset/seek pagination.

Instead of:

```text
page=500
```

use something like:

```text
id > lastSeenId
```

Example:

```sql
SELECT *
FROM orders
WHERE id > 10000
ORDER BY id
LIMIT 20;
```

This can scale better for deep pagination.

Do not replace the project's current pagination mechanism today.

This is a future optimization topic.

---

# 48. ORDER BY and Indexes

Consider:

```sql
SELECT *
FROM orders
WHERE customer_id = 1
ORDER BY id
LIMIT 20;
```

A good index may sometimes help both:

```text
filtering
+
ordering
```

But whether it does depends on:

* index definition
* query structure
* planner estimates
* table size
* data distribution

Again:

> Measure.

---

# 49. Query Planning Mental Model

PostgreSQL is effectively asking:

```text
Which plan costs less?

        ┌── Sequential Scan
Query ──┼── Index Scan
        ├── Bitmap Scan
        ├── Join strategy
        └── Other strategies
```

The planner uses statistics and cost estimates.

---

# 50. Estimated vs Actual Rows

One of the most useful diagnostics is:

```text
estimated rows
vs
actual rows
```

If PostgreSQL estimates:

```text
100 rows
```

but actually gets:

```text
100,000 rows
```

there may be a statistics/data-distribution issue.

Bad estimates can lead to poor plans.

---

# 51. Query Plan Red Flags

Watch for:

```text
Large sequential scans
Huge row-estimation errors
Unexpected nested loops
Large sorts
High buffer reads
Repeated expensive operations
Very high execution time
```

Not every sequential scan is bad.

The context matters.

---

# 52. Index Write Cost

Suppose the table has:

```text
5 indexes
```

An INSERT may require PostgreSQL to update:

```text
table
+
index 1
+
index 2
+
index 3
+
index 4
+
index 5
```

Therefore:

```text
More indexes
    ↓
Potentially slower writes
```

This matters in high-throughput banking systems.

---

# 53. Index Storage Cost

Indexes consume disk space.

Inspect:

```sql
SELECT
    indexrelname,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
WHERE relname = 'orders'
ORDER BY pg_relation_size(indexrelid) DESC;
```

This gives a practical view of index storage.

---

# 54. Index Usage Statistics

Run:

```sql
SELECT
    schemaname,
    relname,
    indexrelname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE relname = 'orders'
ORDER BY indexrelname;
```

Important fields:

```text
idx_scan
idx_tup_read
idx_tup_fetch
```

These help us understand whether indexes are being used.

---

# 55. Caution About Statistics

Index usage statistics are not a universal proof of usefulness.

For example:

```text
Database recently restarted
```

can reset or change accumulated statistics.

Therefore:

```text
Index statistics
+
Query plans
+
Real workload
+
Application metrics
```

provide stronger evidence than one number alone.

---

# 56. Production Consideration — `CREATE INDEX CONCURRENTLY`

For a production database with significant traffic, index creation strategy matters.

PostgreSQL supports:

```sql
CREATE INDEX CONCURRENTLY ...
```

which can reduce blocking of normal writes compared with a regular index build.

However:

> Do not blindly put `CREATE INDEX CONCURRENTLY` inside a normal transactional Flyway migration.

Migration tooling and transaction behavior must be considered deliberately.

For this local learning exercise, a normal:

```sql
CREATE INDEX
```

is sufficient unless the migration strategy explicitly supports the concurrent form.

---

# 57. Flyway Integration

If measurement proves the composite index is justified, it should become a migration.

Create:

```text
src/main/resources/db/migration/V4__add_order_query_indexes.sql
```

Example:

```sql
CREATE INDEX idx_orders_customer_status
    ON orders(customer_id, status);
```

Do not modify:

```text
V1
V2
V3
```

Once a Flyway migration has been applied, treat it as immutable.

---

# 58. Why Use Flyway?

Without Flyway:

```text
Developer A
    ↓
Manual CREATE INDEX

Developer B
    ↓
Different manual change

Production
    ↓
Unknown database state
```

With Flyway:

```text
Git
 ↓
Migration
 ↓
CI
 ↓
Deployment
 ↓
Database
```

The database change becomes version-controlled.

---

# 59. Do Not Add Unjustified Indexes

A tempting migration would be:

```sql
CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_product_name
    ON orders(product_name);

CREATE INDEX idx_orders_created_at
    ON orders(created_at);

CREATE INDEX idx_orders_customer_status
    ON orders(customer_id, status);
```

Do not do this automatically.

That creates:

```text
Index bloat
Write overhead
Storage overhead
Maintenance overhead
```

The project principle is:

> **Every index should have a reason.**

---

# 60. Testing After Index Changes

Indexes should not change business behavior.

Therefore the full test suite must still pass.

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
```

The performance optimization must not break:

```text
CRUD
Filtering
Pagination
Sorting
Transactions
Concurrency
Exception handling
Validation
```

---

# 61. API Smoke Test

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

Open another PowerShell window.

Test:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/orders/search?customerId=1&status=PENDING&page=0&size=20&sort=id" `
    -Method GET
```

Use a customer ID that exists in your local database.

---

# 62. Compare API Behavior

The index should be transparent to the API consumer.

Before:

```text
GET /api/v1/orders/search
```

After:

```text
GET /api/v1/orders/search
```

The contract should remain the same.

Only the database execution strategy changes.

This is an important architectural concept:

> **Infrastructure optimization should not unnecessarily change the API contract.**

---

# 63. Performance Testing Principle

Do not benchmark only once.

A stronger workflow is:

```text
Baseline
   ↓
Change
   ↓
Benchmark
   ↓
Compare
   ↓
Repeat
```

For serious performance engineering, consider:

```text
Cold cache
Warm cache
Representative data volume
Representative query distribution
Concurrent requests
Production-like hardware
```

A tiny local database is not equivalent to production.

---

# 64. Artificial Data Volume

If the local database contains only a handful of orders, index benefits may be difficult to observe.

For learning, you can populate a controlled test dataset.

But do not blindly insert millions of records into an important local database.

Prefer a dedicated test environment or carefully controlled dataset.

---

# 65. Realistic Workload

A banking backend may have:

```text
Millions of customers
Millions/billions of transactions
High concurrent reads
High concurrent writes
Reporting workloads
Operational queries
```

An index strategy that works on:

```text
100 orders
```

may behave differently at:

```text
100 million orders
```

Scale matters.

---

# 66. Query Performance Is a System Property

Performance does not depend only on SQL.

Consider:

```text
API
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Hibernate
 ↓
JDBC
 ↓
PostgreSQL
 ↓
Disk / Memory
```

Performance can be affected by every layer.

Day 15 focuses on the database layer.

---

# 67. JPA Connection

Spring Data JPA may generate SQL from:

```java
findByCustomer_IdAndStatus(...)
```

or:

```java
Specification<Order>
```

The developer therefore needs to understand the SQL that the ORM ultimately produces.

Do not assume:

> "Hibernate handles it, so database performance is not my problem."

Professional backend developers understand both:

```text
Java abstraction
+
generated SQL
+
database execution plan
```

---

# 68. Specification Connection

Day 12 introduced dynamic Specifications.

A request may produce:

```text
customerId
+
status
+
productName
+
quantity
```

The generated SQL may become increasingly complex.

That makes query planning and indexing increasingly important.

---

# 69. Pagination Connection

Day 10 introduced:

```text
Pageable
Page<T>
```

The database ultimately performs pagination operations.

Therefore:

```text
Spring Pageable
        ↓
SQL LIMIT/OFFSET
        ↓
Database execution
```

Application-level pagination does not eliminate database performance concerns.

---

# 70. Fetching Connection

Day 11 introduced:

```text
JOIN FETCH
```

Query performance therefore has multiple dimensions:

```text
Filtering
+
Joining
+
Fetching
+
Sorting
+
Pagination
+
Indexing
```

Day 15 focuses specifically on indexes and execution plans.

---

# 71. Transaction Connection

Day 14 introduced transactions.

A slow query inside a transaction can hold resources longer.

Conceptually:

```text
Transaction begins
      ↓
Slow query
      ↓
Locks/resources remain longer
      ↓
Transaction commits
```

Therefore query performance can affect concurrency and throughput.

This is another reason database optimization matters.

---

# 72. Banking Connection

Imagine:

```text
Transfer request
   ↓
Transaction
   ↓
Check account
   ↓
Check balance
   ↓
Write transaction
   ↓
Update ledger
   ↓
Commit
```

If database queries are poorly optimized:

```text
Latency ↑
Connection usage ↑
Transaction duration ↑
Contention ↑
Throughput ↓
```

Database performance is therefore part of reliability.

---

# 73. Security Connection

Never optimize by bypassing important database integrity controls.

Do not remove:

```text
FOREIGN KEY
UNIQUE
NOT NULL
```

just because constraints appear to have a performance cost.

Security and integrity come before premature optimization.

---

# 74. Observability Connection

Eventually we want database metrics such as:

```text
Query latency
Slow queries
Connection pool usage
Database CPU
I/O
Cache hit ratio
Index usage
Lock contention
```

Day 15 establishes the foundation for understanding those metrics.

---

# 75. Reliability-Driven Connection

Poor database performance can become a reliability problem.

```text
Slow query
   ↓
Connection held longer
   ↓
Pool exhaustion
   ↓
Requests queue
   ↓
Timeouts
   ↓
Service degradation
```

Performance and reliability are connected.

---

# 76. Measure First

This is the most important principle of Day 15.

Before:

```text
CREATE INDEX
```

ask:

```text
What query is slow?

How slow?

How often does it execute?

What is the execution plan?

How selective is the filter?

What happens after the index?

Does the improvement justify the cost?
```

---

# 77. Day 15 Practical Workflow

Use this exact sequence:

```text
1. Check Git
2. Run tests
3. Inspect schema
4. Inspect existing indexes
5. Identify real query patterns
6. Measure baseline
7. Inspect data distribution
8. ANALYZE table
9. Evaluate candidate index
10. Create candidate index
11. Measure again
12. Compare plans
13. Decide whether index is justified
14. Convert justified change into Flyway migration
15. Run full tests
16. Smoke test API
17. Review Git diff
18. Commit
19. Push
```

---

# 78. Candidate Index Decision

The most natural candidate for this project is:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

Why?

Because the application supports:

```text
customerId
+
status
```

filtering.

But the final decision must be based on:

```text
EXPLAIN
EXPLAIN ANALYZE
BUFFERS
Data distribution
Query frequency
Table size
Write workload
```

---

# 79. When to Keep the Index

Keep the index if evidence shows meaningful benefit such as:

```text
Lower execution time
Reduced buffer work
Better execution plan
Frequent production query
Meaningful scalability benefit
Acceptable write/storage cost
```

---

# 80. When Not to Keep the Index

Do not keep it simply because:

```text
"Indexes are good."
```

Remove/avoid it if:

```text
No meaningful benefit
Query is rare
Table is tiny
Planner never uses it
Write overhead outweighs benefit
Storage cost is unjustified
```

---

# 81. Removing a Test Index

If you created an experimental index manually and decide it is not justified:

```sql
DROP INDEX IF EXISTS idx_orders_customer_status;
```

Then verify:

```sql
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'orders'
ORDER BY indexname;
```

Do not leave experimental database changes undocumented.

---

# 82. If the Index Is Justified

Create:

```text
src/main/resources/db/migration/V4__add_order_query_indexes.sql
```

Example:

```sql
CREATE INDEX idx_orders_customer_status
    ON orders(customer_id, status);
```

Then let Flyway own the change.

Do not manually create the production index and also create a migration that assumes it does not exist.

The database state and migration history must remain consistent.

---

# 83. Verify Flyway

After adding the migration, run the application or Flyway migration process and verify:

```sql
SELECT
    version,
    description,
    type,
    installed_on,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

You should see the new migration.

---

# 84. Full Test

Run:

```powershell
.\mvnw.cmd clean test
```

Do not accept:

```text
BUILD SUCCESS
```

if tests were silently skipped unexpectedly.

Review the Maven output.

---

# 85. Build

Run:

```powershell
.\mvnw.cmd -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

# 86. Git Review

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

If you created a migration, carefully review it.

---

# 87. Stage

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

---

# 88. Commit

Use:

```powershell
git commit -m "perf(db): optimize order query indexes"
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

# 89. Day 15 Definition of Done

```text
[ ] Understand database indexes
[ ] Understand B-tree indexes
[ ] Understand selectivity
[ ] Understand cardinality
[ ] Understand query planner
[ ] Understand execution plans
[ ] Understand EXPLAIN
[ ] Understand EXPLAIN ANALYZE
[ ] Understand BUFFERS
[ ] Understand sequential scans
[ ] Understand index scans
[ ] Understand bitmap scans
[ ] Understand estimated vs actual rows
[ ] Understand composite indexes
[ ] Understand composite index column ordering
[ ] Understand leftmost-prefix behavior
[ ] Understand pagination performance
[ ] Understand OFFSET cost
[ ] Understand index write overhead
[ ] Understand index storage cost
[ ] Understand index usage statistics
[ ] Inspect existing orders indexes
[ ] Measure baseline queries
[ ] Measure customer filtering
[ ] Measure status filtering
[ ] Measure customer + status filtering
[ ] Measure pagination query
[ ] Analyze data distribution
[ ] Run ANALYZE
[ ] Evaluate composite index
[ ] Compare before/after
[ ] Avoid unjustified indexes
[ ] Convert justified change into Flyway migration
[ ] Full Maven tests pass
[ ] API smoke test passes
[ ] Documentation saved
[ ] Git commit created
[ ] Git push completed
[ ] Working tree clean
```

---

# 90. Engineering Principles Applied

## SOLID

Database optimization should remain within the persistence/infrastructure responsibility.

---

## DRY

Do not create multiple indexes serving essentially the same query pattern.

---

## KISS

Start with the simplest index strategy that solves the measured problem.

---

## YAGNI

Do not index every column.

---

## Separation of Concerns

```text
API
 ↓
Service
 ↓
Repository
 ↓
SQL
 ↓
Database
```

Database optimization belongs primarily to the persistence/database concern.

---

## High Cohesion

Indexes should have clear workload-driven purposes.

---

## Low Coupling

The API should not depend on a particular physical index implementation.

---

## Encapsulation

The API consumer should not care whether PostgreSQL uses:

```text
Seq Scan
Index Scan
Bitmap Scan
```

---

## Fail Fast

Detect query/data problems early through tests and execution-plan analysis.

---

## Least Privilege

Database users should eventually receive only the permissions required for their role.

---

## Design for Failure

Slow queries can cause:

```text
Connection exhaustion
Timeouts
Lock contention
Service degradation
```

Performance is therefore part of reliability.

---

## Idempotency

Index creation migrations must be controlled through Flyway rather than ad-hoc repeated operations.

---

## Observability

Execution plans, database metrics, query latency, and index usage provide operational evidence.

---

## Measure First

The defining Day 15 principle:

```text
Measure
 ↓
Understand
 ↓
Optimize
 ↓
Measure Again
```

---

# 91. TDD Connection

For database performance work, TDD is complemented by measurement.

Functional requirement:

```text
Order search must return correct results.
```

Performance requirement:

```text
Order search must execute efficiently
under representative workload.
```

Tests verify correctness.

Performance measurements verify efficiency.

Both matter.

---

# 92. DDD Connection

DDD identifies business concepts.

Database indexing supports efficient access to those concepts.

For example:

```text
Customer
Order
```

are domain concepts.

The index:

```text
(customer_id, status)
```

is an infrastructure optimization supporting an access pattern.

Do not allow database indexes to dictate the domain model.

---

# 93. Contract-First Connection

The API remains:

```text
GET /api/v1/orders/search
```

before and after the index.

The database optimization should normally be invisible to the API consumer.

---

# 94. Event-Driven Connection

Later, events may produce large volumes of:

```text
OrderCreated
OrderUpdated
OrderCancelled
```

Database read/write patterns will become more important.

Indexing decisions must consider both:

```text
Read workload
+
Write workload
```

---

# 95. Security-Driven Connection

Do not sacrifice:

```text
Foreign keys
Constraints
Authorization
Auditability
Data integrity
```

for premature performance gains.

Optimization must preserve security and correctness.

---

# 96. Reliability-Driven Connection

The chain is:

```text
Query performance
      ↓
Transaction duration
      ↓
Connection usage
      ↓
Concurrency
      ↓
Throughput
      ↓
Reliability
```

Database performance is therefore not merely a technical optimization.

It affects system reliability.

---

# 97. DevOps-Driven Connection

The complete database change lifecycle is:

```text
Measure
 ↓
Implement
 ↓
Test
 ↓
Migration
 ↓
Git
 ↓
CI
 ↓
Deployment
 ↓
Observe
```

A database optimization should be reproducible across environments.

---

# 98. Notebook Quick Reference

## Index

```text
Index
= data structure that helps locate rows efficiently.
```

---

## Main index type

```text
B-tree
```

Good for many:

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

## Query planner

```text
SQL
 ↓
Planner
 ↓
Execution Plan
 ↓
Executor
```

---

## EXPLAIN

```sql
EXPLAIN
SELECT ...
```

Shows planned execution.

---

## EXPLAIN ANALYZE

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT ...
```

Executes the query and provides actual execution information.

---

## Composite index

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

Column order matters.

---

## Current project query

```sql
SELECT *
FROM orders
WHERE customer_id = ?
  AND status = ?;
```

Potential index:

```text
(customer_id, status)
```

But:

> **Measure before keeping it.**

---

## Pagination

```sql
LIMIT 20 OFFSET 10000
```

Large offsets can become expensive.

Future alternative:

```sql
WHERE id > :lastSeenId
ORDER BY id
LIMIT 20
```

This is keyset/seek pagination.

---

## Index costs

```text
Storage
+
Memory
+
INSERT cost
+
UPDATE cost
+
DELETE cost
```

---

## Most important principle

```text
Measure First
     ↓
Understand
     ↓
Optimize
     ↓
Measure Again
```

---

# 99. Day 15 Mental Model

Remember:

```text
                    API
                     │
                     ▼
              Order Search
                     │
                     ▼
                  Service
                     │
                     ▼
                Repository
                     │
                     ▼
                    SQL
                     │
                     ▼
             PostgreSQL Planner
                     │
             ┌───────┼────────┐
             │       │        │
             ▼       ▼        ▼
          Seq Scan Index   Bitmap
                     │
                     ▼
                  Result
```

The important question is not:

> "Did we create an index?"

The important question is:

> **"Did the measured workload become better enough to justify the index?"**

---

# 100. Day 15 Final Takeaway

Database performance engineering is evidence-driven.

The professional workflow is:

```text
Real Query
    ↓
Baseline
    ↓
EXPLAIN
    ↓
EXPLAIN ANALYZE
    ↓
Understand Plan
    ↓
Understand Data Distribution
    ↓
Candidate Index
    ↓
Measure Again
    ↓
Compare
    ↓
Keep or Reject
    ↓
Version Through Flyway
```

Do not optimize databases by intuition alone.

The project has now moved from:

```text
CRUD
```

to:

```text
Correctness
+
Transactions
+
Concurrency
+
Performance
```

That is an important transition from beginner backend development toward professional enterprise engineering.

**Day 15 principle:**

> **The fastest database query is not the one with the most indexes; it is the query executed by the right plan for the actual workload.**
