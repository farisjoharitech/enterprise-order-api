# Day 15 — Database Indexing & Query Performance

## Goal

Learn to optimize PostgreSQL queries using evidence rather than guessing.

## Core Rule

> Measure First.

Workflow:

```text
Identify query
→ Measure
→ EXPLAIN
→ EXPLAIN ANALYZE
→ Understand workload
→ Design index
→ Apply migration
→ Measure again
→ Compare
```

## Index

An index is a database data structure that can provide a faster access path to rows.

Common PostgreSQL index:

```sql
CREATE INDEX idx_orders_customer_id
ON orders(customer_id);
```

## B-Tree

PostgreSQL commonly uses B-tree indexes.

Useful for:

```text
=
<
>
<=
>=
BETWEEN
ORDER BY
```

## Sequential Scan

```text
Seq Scan
```

Database scans table rows/pages.

Not automatically bad.

Small tables may be faster with sequential scan.

## Index Scan

```text
Index
→ matching row locations
→ table rows
```

Can be useful for selective queries.

## Bitmap Scan

PostgreSQL may use:

```text
Bitmap Index Scan
→ Bitmap Heap Scan
```

Useful when many rows match.

## EXPLAIN

```sql
EXPLAIN
SELECT *
FROM orders
WHERE customer_id = 1;
```

Shows estimated execution plan.

## EXPLAIN ANALYZE

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1;
```

Executes query and shows actual execution information.

## Selectivity

How much a filter reduces the result set.

High selectivity:

```text
1,000,000 rows
→ 100 rows
```

Low selectivity:

```text
1,000,000 rows
→ 800,000 rows
```

## Cardinality

Number of distinct values.

Example:

```text
status → low cardinality
customer_id → higher cardinality
```

## Composite Index

Example:

```sql
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

Useful candidate for:

```sql
WHERE customer_id = ?
AND status = ?
```

## Column Order

These are different:

```text
(customer_id, status)
(status, customer_id)
```

Leading column matters.

## Current Project Query

The application already has:

```java
findByCustomer_IdAndStatus(...)
```

Therefore:

```sql
WHERE customer_id = ?
AND status = ?
```

is a real application workload.

## Existing Index

Do not duplicate the existing:

```text
idx_orders_customer_id
```

## Flyway

Database changes must be versioned.

Day 15:

```text
V4__add_order_query_indexes.sql
```

Potential migration:

```sql
CREATE INDEX idx_orders_customer_status
    ON orders(customer_id, status);
```

Only add the index if measurements justify it.

## Index Tradeoff

Indexes improve read access but have costs:

```text
INSERT
UPDATE
DELETE
storage
maintenance
```

Therefore:

```text
More indexes ≠ better database
```

## Pagination

Existing application uses:

```text
page
size
sort
```

Large OFFSET values can become expensive:

```sql
LIMIT 20 OFFSET 10000
```

Later topic:

```text
Keyset / Cursor Pagination
```

## Important Principles

### Measure First

Never optimize without evidence.

### YAGNI

Do not add indexes for hypothetical queries.

### KISS

Prefer the smallest index strategy that solves the real problem.

### DRY

Do not create duplicate indexes.

### SoC

Java application logic should not contain database performance hacks unnecessarily.

## Useful Commands

Project:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Tests:

```powershell
.\mvnw.cmd clean test
```

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Git:

```powershell
git status
git diff
git diff --stat
```

PostgreSQL:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" `
  -h localhost `
  -p 5433 `
  -U postgres `
  -d enterprise_order_db
```

Indexes:

```sql
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'orders'
ORDER BY indexname;
```

Query plan:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM orders
WHERE customer_id = 1
AND status = 'PENDING';
```

Index statistics:

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

## Mental Model

```text
API
 ↓
JPA
 ↓
SQL
 ↓
PostgreSQL Planner
 ↓
Execution Plan
 ↓
Index / Scan
 ↓
Rows
```

Senior backend developers must understand the entire path.
