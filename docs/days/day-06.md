# Day 6 — Database Schema & Relationships

## Objective

Introduce Customer -> Order relational modeling.

## Database

### customers

- id — primary key
- name — required
- email — required and unique

### orders

- id — primary key
- customer_id — foreign key
- product_name
- quantity
- status

## Relationship

Customer 1 ---- * Order

JPA:

@ManyToOne(fetch = FetchType.LAZY)

@JoinColumn(name = "customer_id")

## Concepts Learned

- Primary key
- Foreign key
- NOT NULL
- UNIQUE
- CHECK
- Index
- Normalization
- JOIN
- Referential integrity
- JPA relationships
- Lazy loading

## SQL

SELECT
WHERE
ORDER BY
LIMIT
INSERT
UPDATE
DELETE
JOIN

## Architecture

Controller
↓
Service
↓
Repository
↓
Spring Data JPA
↓
Hibernate
↓
PostgreSQL

## Key Lesson

Application validation protects API input.

Database constraints protect persisted data.

Both are required.

## Validation

- [ ] Customer entity works
- [ ] Customer repository works
- [ ] Order -> Customer relationship works
- [ ] customer_id FK exists
- [ ] duplicate email rejected
- [ ] invalid customer reference rejected
- [ ] JOIN verified
- [ ] API verified
- [ ] persistence survives restart
- [ ] targeted tests pass
- [ ] full tests pass