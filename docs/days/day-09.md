# Day 9 — DTO Mapping & API/Domain Separation

## Objective

Separate REST API contracts from JPA persistence entities.

## Core Principle

```text
DTO ≠ Entity
```

DTOs represent the API contract.

Entities represent the persistence model.

## Request Flow

```text
HTTP Request
    ↓
Request DTO
    ↓
Controller
    ↓
Service
    ↓
JPA Entity
    ↓
Repository
    ↓
PostgreSQL
```

## Response Flow

```text
PostgreSQL
    ↓
Repository
    ↓
JPA Entity
    ↓
Service
    ↓
Response DTO
    ↓
Controller
    ↓
JSON
```

## Why Not Return Entities Directly?

Returning JPA entities directly can cause:

* Persistence model leakage
* API/database coupling
* Lazy-loading problems
* N+1 query risks
* Accidental exposure of fields
* Unstable API contracts
* Circular serialization problems

## Day 6 API Contract

Order requests use:

```json
{
    "productName": "Mechanical Keyboard",
    "quantity": 2,
    "customerId": 1,
    "status": "PENDING"
}
```

The API uses `customerId`, not `customerName`, as the relational identifier.

## DTO Responsibilities

### Request DTO

Represents data received from the client.

```text
HTTP → Request DTO
```

### Response DTO

Represents data returned to the client.

```text
Response DTO → HTTP
```

### JPA Entity

Represents persistence.

```text
Entity ↔ Database
```

## Mapping

Simple explicit mapping is sufficient at the current project size.

Example concept:

```java
private OrderResponse toResponse(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getProductName(),
        order.getQuantity(),
        order.getCustomer().getId(),
        order.getStatus()
    );
}
```

The exact implementation follows the existing DTO structure.

## Mapping Libraries

No mapping library was added.

Do not introduce MapStruct or ModelMapper before the project requires it.

## Banking Relevance

DTO separation is especially important in banking systems.

Internal entities may contain sensitive or internal fields such as:

```text
passwordHash
internalRiskScore
complianceStatus
internalNotes
```

These must not automatically become API fields.

The API should explicitly control which information is exposed.

## Architecture

```text
Client
  ↓
Request DTO
  ↓
Controller
  ↓
Service
  ↓
JPA Entity
  ↓
Repository
  ↓
Hibernate
  ↓
PostgreSQL
```

Response:

```text
PostgreSQL
  ↓
Entity
  ↓
Service
  ↓
Response DTO
  ↓
Controller
  ↓
JSON
```

## Key Lessons

1. DTOs define API contracts.
2. JPA entities represent persistence.
3. Do not expose JPA entities directly through REST.
4. Keep controllers thin.
5. Mapping belongs around the application/service boundary.
6. Preserve `customerId` as the Order API relationship identifier.
7. Avoid unnecessary mapping frameworks.
8. DTO separation improves security and API stability.
9. Database models can evolve without automatically changing API contracts.

## Day 9 Definition of Done

* [ ] DTO/entity separation implemented
* [ ] Existing API behavior preserved
* [ ] GET tested
* [ ] POST tested
* [ ] Validation tested
* [ ] 404 tested
* [ ] Entity internals not exposed
* [ ] Automated tests pass
* [ ] Clean tests pass
* [ ] Git diff reviewed
* [ ] Documentation completed
* [ ] Commit created
* [ ] GitHub pushed
