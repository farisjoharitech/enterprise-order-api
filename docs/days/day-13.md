\# Day 13 — Database Schema Migrations with Flyway



\## Objective



Introduce controlled database schema evolution using Flyway.



The project previously relied on Hibernate schema evolution. Day 13 moves database changes toward explicit, version-controlled migrations.



\---



\## Project



\*\*Enterprise Order API\*\*



The same application continues to evolve from previous days.



Day 13 adds database migration management and introduces an `orders.created\_at` column.



\---



\## Topics Learned



\* Database schema migration

\* Flyway

\* Versioned SQL migrations

\* Flyway baseline

\* Existing database adoption

\* Migration history

\* Migration immutability

\* Schema validation

\* Database backfill

\* `NOT NULL`

\* `DEFAULT CURRENT\_TIMESTAMP`

\* PostgreSQL `TIMESTAMP WITH TIME ZONE`

\* H2 test database strategy

\* PostgreSQL migration verification

\* Testcontainers migration strategy

\* Expand/Migrate/Verify/Switch/Contract



\---



\# 1. Why Database Migrations?



Database schemas evolve throughout the lifetime of an enterprise application.



Examples:



```text

Day 1

Create customers



Day 5

Create orders



Day 6

Add customer relationship



Day 13

Add orders.created\_at



Future

Add indexes

Add columns

Rename columns

Remove legacy columns

Change constraints

```



Changing a production database manually is dangerous.



Flyway provides a controlled migration history.



\---



\# 2. Flyway Migration Directory



Migrations are stored in:



```text

src/main/resources/db/migration/

```



Current migrations:



```text

V1\_\_baseline.sql

V2\_\_add\_created\_at\_to\_orders.sql

```



Flyway naming convention:



```text

V<version>\_\_<description>.sql

```



Example:



```text

V2\_\_add\_created\_at\_to\_orders.sql

```



contains:



```text

Version:

2



Description:

add created at to orders

```



\---



\# 3. V1 Baseline



The project adopted Flyway after the PostgreSQL database already existed.



Therefore V1 is a baseline marker rather than a database creation script.



Current file:



```sql

\-- Existing schema baseline.

\-- The database was created before Flyway adoption.

\-- Version 1 represents the schema state at the beginning of Day 13.

```



V1 does not create the tables.



Instead, Flyway establishes:



```text

Existing database

&#x20;       ↓

Baseline at V1

```



This allows Flyway to manage future changes without recreating the existing schema.



\---



\# 4. Flyway Configuration



Application configuration:



```properties

spring.flyway.enabled=true

spring.flyway.baseline-on-migrate=true

spring.flyway.baseline-version=1

```



Meaning:



\### `spring.flyway.enabled`



Enables Flyway in the application.



\### `baseline-on-migrate`



Allows Flyway to baseline an existing non-empty database that does not yet have Flyway history.



\### `baseline-version`



Defines the baseline version:



```text

1

```



\---



\# 5. V2 Migration



Migration:



```text

V2\_\_add\_created\_at\_to\_orders.sql

```



SQL:



```sql

ALTER TABLE orders

&#x20;   ADD COLUMN created\_at TIMESTAMP WITH TIME ZONE;



UPDATE orders

SET created\_at = CURRENT\_TIMESTAMP

WHERE created\_at IS NULL;



ALTER TABLE orders

&#x20;   ALTER COLUMN created\_at SET DEFAULT CURRENT\_TIMESTAMP;



ALTER TABLE orders

&#x20;   ALTER COLUMN created\_at SET NOT NULL;

```



\---



\# 6. V2 Migration Explained



\## Step 1 — Add column



```sql

ALTER TABLE orders

&#x20;   ADD COLUMN created\_at TIMESTAMP WITH TIME ZONE;

```



The new column initially allows existing rows to be populated.



\---



\## Step 2 — Backfill existing data



```sql

UPDATE orders

SET created\_at = CURRENT\_TIMESTAMP

WHERE created\_at IS NULL;

```



Existing orders receive a timestamp.



This is important when introducing a new required column into an existing table.



\---



\## Step 3 — Add default



```sql

ALTER TABLE orders

&#x20;   ALTER COLUMN created\_at SET DEFAULT CURRENT\_TIMESTAMP;

```



New orders automatically receive a timestamp when the application does not provide one.



\---



\## Step 4 — Enforce NOT NULL



```sql

ALTER TABLE orders

&#x20;   ALTER COLUMN created\_at SET NOT NULL;

```



After existing data has been populated, the column can safely become mandatory.



\---



\# 7. Enterprise Migration Pattern



The migration follows an important production database evolution pattern:



```text

EXPAND

&#x20;  ↓

MIGRATE

&#x20;  ↓

VERIFY

&#x20;  ↓

SWITCH

&#x20;  ↓

CONTRACT

```



For Day 13:



```text

EXPAND

Add created\_at



&#x20;   ↓



MIGRATE

Populate existing rows



&#x20;   ↓



VERIFY

Check timestamps and constraints



&#x20;   ↓



SWITCH

Application can use created\_at



&#x20;   ↓



CONTRACT

Future cleanup of obsolete structures

```



We intentionally do not perform unnecessary destructive changes today.



\---



\# 8. PostgreSQL Verification



Flyway reported:



```text

Schema version: 2

```



Migration history:



```text

Version 1

<< Flyway Baseline >>



Version 2

add created at to orders

Success

```



Both migration history entries were successful.



\---



\# 9. Final Database Schema Verification



The `orders` table contains:



```text

id

customer\_name

product\_name

quantity

status

customer\_id

created\_at

```



The `created\_at` column was verified as:



```text

data\_type:

timestamp with time zone



is\_nullable:

NO



default:

CURRENT\_TIMESTAMP

```



Therefore the migration produced the intended schema.



\---



\# 10. Automatic Timestamp Verification



A temporary customer and order were created.



The order was inserted without specifying `created\_at`.



PostgreSQL generated:



```text

2026-09-07 15:14:14.216835+08

```



This verified:



```text

INSERT

&#x20; ↓

created\_at omitted

&#x20; ↓

PostgreSQL DEFAULT

&#x20; ↓

CURRENT\_TIMESTAMP

&#x20; ↓

created\_at populated

```



The temporary verification data was removed afterward.



\---



\# 11. H2 Test Strategy



H2 is used for fast application tests.



The H2-based tests use:



```properties

spring.jpa.hibernate.ddl-auto=create-drop

spring.flyway.enabled=false

```



Reason:



The Day 13 V1 migration is a baseline marker and intentionally does not create the application schema.



An empty H2 database would therefore fail when V2 attempts:



```sql

ALTER TABLE orders

```



because `orders` does not exist.



Therefore H2 application tests use Hibernate to create the temporary test schema.



\---



\# 12. PostgreSQL Migration Testing



Flyway migration correctness should ultimately be verified against PostgreSQL.



The preferred architecture is:



```text

Application tests

&#x20;      ↓

H2

&#x20;      ↓

Fast feedback



Migration tests

&#x20;      ↓

PostgreSQL Testcontainers

&#x20;      ↓

Real PostgreSQL behavior

```



The project already contains Testcontainers integration tests.



At Day 13, Docker/Testcontainers is unavailable on the development machine, resulting in 6 skipped tests.



This is treated as an environment limitation rather than a reason to weaken the tests.



\---



\# 13. Flyway Maven Verification



The Flyway Maven command was executed using Java 17:



```powershell

$env:JAVA\_HOME = "C:\\Program Files\\Java\\jdk-17"

```



Command:



```powershell

.\\mvnw.cmd flyway:info "-Dflyway.url=jdbc:postgresql://localhost:5433/enterprise\_order\_db" "-Dflyway.user=postgres" "-Dflyway.password=admin"

```



Result:



```text

Schema version: 2

```



and:



```text

V1  << Flyway Baseline >>       Baseline

V2  add created at to orders    Success

```



The Maven build completed successfully.



Passwords must never be committed to Git.



\---



\# 14. Important Flyway Lesson



Applied migrations should be treated as immutable.



Once:



```text

V2\_\_add\_created\_at\_to\_orders.sql

```



has successfully executed in a database, it should not be modified simply to fix a test.



If the test strategy is wrong, fix the test strategy.



Do not silently rewrite migration history.



\---



\# 15. Java/Maven Environment Lesson



Maven initially used Java 8 because:



```text

JAVA\_HOME

```



pointed to:



```text

C:\\Program Files\\Java\\jdk1.8.0\_351

```



The project requires Java 17.



Temporary PowerShell configuration:



```powershell

$env:JAVA\_HOME = "C:\\Program Files\\Java\\jdk-17"

```



Verified Maven environment:



```text

Apache Maven 3.9.16

Java version: 17.0.12

runtime: C:\\Program Files\\Java\\jdk-17

```



\---



\# 16. Testing Result



Final Maven test result:



```text

Tests run: 56

Failures: 0

Errors: 0

Skipped: 6

```



Therefore:



```text

Failures = 0

Errors = 0

```



The six skipped tests are related to the existing Docker/Testcontainers environment.



\---



\# 17. Useful Commands



\## Set Java 17



```powershell

$env:JAVA\_HOME = "C:\\Program Files\\Java\\jdk-17"

```



\## Run tests



```powershell

.\\mvnw.cmd clean test

```



\## Check Flyway



```powershell

.\\mvnw.cmd flyway:info "-Dflyway.url=jdbc:postgresql://localhost:5433/enterprise\_order\_db" "-Dflyway.user=postgres" "-Dflyway.password=admin"

```



\## Connect to PostgreSQL



```powershell

\& "C:\\Program Files\\PostgreSQL\\17\\bin\\psql.exe" -h localhost -p 5433 -U postgres -d enterprise\_order\_db

```



\## Check migration history



```sql

SELECT installed\_rank,

&#x20;      version,

&#x20;      description,

&#x20;      script,

&#x20;      success

FROM flyway\_schema\_history

ORDER BY installed\_rank;

```



\## Check schema



```sql

\\d orders

```



\## Check created\_at



```sql

SELECT column\_name,

&#x20;      data\_type,

&#x20;      is\_nullable,

&#x20;      column\_default

FROM information\_schema.columns

WHERE table\_name = 'orders'

ORDER BY ordinal\_position;

```



\---



\# 18. Day 13 Summary



Today I learned how to introduce Flyway into an existing database without recreating the database.



I learned the difference between a baseline migration and a normal versioned migration.



I implemented:



```text

Flyway

&#x20;  ↓

V1 baseline

&#x20;  ↓

V2 migration

&#x20;  ↓

orders.created\_at

```



I verified the migration directly against PostgreSQL.



I also verified that PostgreSQL automatically creates `created\_at` using `CURRENT\_TIMESTAMP`.



Application tests were configured separately from migration testing so that fast H2 tests do not depend on a baseline-only migration.



The final test suite completed with:



```text

56 tests

0 failures

0 errors

6 skipped

```



\---



\# Day 13 Completion



\* \[x] Flyway dependency configured

\* \[x] Spring Boot Flyway integration enabled

\* \[x] Existing PostgreSQL database baselined

\* \[x] V1 baseline created

\* \[x] V2 migration created

\* \[x] `created\_at` added

\* \[x] Existing data migration supported

\* \[x] NOT NULL constraint verified

\* \[x] DEFAULT verified

\* \[x] PostgreSQL migration verified

\* \[x] H2 test strategy corrected

\* \[x] Maven test suite passed

\* \[x] Migration history verified

\* \[x] Daily notes documented



\## Git Commit



Planned commit:



```text

feat(db): add flyway database migrations

```



