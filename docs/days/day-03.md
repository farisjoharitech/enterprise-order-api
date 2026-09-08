# Day 3 — Spring Boot Project Structure & Dependency Injection

**Project:** `enterprise-order-api`
**Learning Track:** Java Enterprise Backend / Banking Backend Mastery
**Day:** 3 / 365
**Primary Focus:** Spring Boot architecture, IoC, Dependency Injection, component scanning, beans, constructor injection, and clean layering

---

## 1. Day 3 Objective

Today we strengthen the internal architecture of the existing `enterprise-order-api`.

The goal is not to create another application.

Everything continues inside the same evolving project.

Today we learn how Spring Boot creates and connects application components using:

* Spring Boot
* Spring IoC Container
* Dependency Injection
* Spring Beans
* Component Scanning
* `@Component`
* `@Service`
* `@Repository`
* `@RestController`
* Constructor Injection
* Layered Architecture
* Separation of Concerns
* Interface-based design
* Dependency direction
* Application startup
* Bean lifecycle fundamentals

The important mental model is:

```text
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Spring is responsible for creating and connecting these objects.

---

# 2. Project Context

The application is an enterprise-style Order API.

Current conceptual domain:

```text
Customer
    |
    | places
    ↓
Order
```

The backend exposes REST APIs for managing customers and orders.

The architecture is intentionally layered.

```text
┌─────────────────────────────┐
│       REST Controller       │
│  HTTP / JSON / Validation   │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│          Service            │
│ Business Rules / Use Cases  │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│        Repository           │
│      Data Access            │
└──────────────┬──────────────┘
               ↓
┌─────────────────────────────┐
│       PostgreSQL             │
└─────────────────────────────┘
```

Spring connects these layers.

---

# 3. What Problem Does Spring Solve?

Without Dependency Injection, code often looks like:

```java
public class OrderService {

    private final OrderRepository repository;

    public OrderService() {
        this.repository = new OrderRepository();
    }
}
```

The service is responsible for constructing its dependency.

This creates tight coupling.

Instead:

```java
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

Now another component provides the repository.

Spring performs that wiring.

Conceptually:

```text
Spring Container

OrderRepository object
        │
        │ inject
        ↓
OrderService object
        │
        │ inject
        ↓
OrderController object
```

The application classes describe what they need.

Spring determines how the objects are created and connected.

---

# 4. Inversion of Control

## Definition

**Inversion of Control (IoC)** means that application code does not completely control the creation and management of its dependencies.

Normally:

```text
Application
    ↓
creates objects
    ↓
uses objects
```

With Spring:

```text
Spring Container
    ↓
creates objects
    ↓
injects dependencies
    ↓
application uses objects
```

The control over object creation is inverted.

---

# 5. Dependency Injection

Dependency Injection is one way Spring implements IoC.

A dependency is an object required by another object.

Example:

```java
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

`OrderService` depends on:

```text
OrderRepository
```

Instead of constructing it internally, the dependency is supplied through the constructor.

That is Dependency Injection.

---

# 6. Constructor Injection

Constructor injection is the preferred style for required dependencies.

Example:

```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

Spring sees:

```text
@Service
OrderService
    +
constructor requiring OrderRepository
```

Spring finds the `OrderRepository` bean and injects it.

---

# 7. Why Constructor Injection?

Constructor injection provides several benefits.

## 7.1 Required dependencies are explicit

The constructor tells us:

```java
public OrderService(OrderRepository orderRepository)
```

This immediately communicates:

> OrderService cannot operate without OrderRepository.

---

## 7.2 Dependencies can be final

```java
private final OrderRepository orderRepository;
```

The dependency cannot accidentally be replaced later.

---

## 7.3 Easier unit testing

We can create the service directly:

```java
OrderRepository repository = mock(OrderRepository.class);

OrderService service =
        new OrderService(repository);
```

No Spring context is required.

---

## 7.4 Better design visibility

A class with ten constructor dependencies is immediately suspicious.

Constructor injection exposes that design problem.

This helps identify:

```text
Low cohesion
Too many responsibilities
Potential God object
Poor separation of concerns
```

---

# 8. Field Injection

Avoid:

```java
@Autowired
private OrderRepository orderRepository;
```

Although Spring supports field injection, it hides dependencies.

The class appears to have no constructor requirements.

Problems include:

* hidden dependencies
* harder unit testing
* mutable dependency fields
* weaker encapsulation
* harder reasoning about object creation

Prefer:

```java
private final OrderRepository orderRepository;

public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
}
```

---

# 9. Setter Injection

Setter injection can be appropriate for optional dependencies.

Example:

```java
public void setSomething(SomeDependency dependency) {
    this.dependency = dependency;
}
```

But required application dependencies should normally use constructor injection.

Mental rule:

```text
Required dependency
        ↓
Constructor injection

Optional/configurable dependency
        ↓
Setter/configuration where appropriate
```

---

# 10. Spring Bean

A **Spring Bean** is an object managed by the Spring IoC container.

Examples:

```java
@Service
public class OrderService {
}
```

```java
@Repository
public interface OrderRepository
        extends JpaRepository<Order, Long> {
}
```

```java
@RestController
public class OrderController {
}
```

Spring detects these components and manages them.

---

# 11. Component Scanning

Spring Boot scans the application's package structure to discover components.

For example:

```text
com.faris.enterprise_order_api
│
├── EnterpriseOrderApiApplication.java
│
├── controller
├── service
├── repository
├── model
└── dto
```

If the main application class is:

```java
package com.faris.enterprise_order_api;
```

Spring scans that package and its subpackages.

Therefore:

```text
com.faris.enterprise_order_api.controller
com.faris.enterprise_order_api.service
com.faris.enterprise_order_api.repository
```

can be discovered automatically.

---

# 12. `@SpringBootApplication`

The main application class normally contains:

```java
@SpringBootApplication
public class EnterpriseOrderApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(
            EnterpriseOrderApiApplication.class,
            args
        );
    }
}
```

`@SpringBootApplication` combines several important Spring features.

Conceptually:

```text
@SpringBootApplication
        │
        ├── Configuration
        ├── Component Scanning
        └── Auto Configuration
```

This is one of the reasons Spring Boot requires relatively little configuration.

---

# 13. `@Component`

Generic Spring-managed component:

```java
@Component
public class SomeComponent {
}
```

Use it when the class is a Spring-managed component but does not specifically represent:

* a service
* a repository
* a controller

Example:

```java
@Component
public class OrderNumberGenerator {
}
```

---

# 14. `@Service`

Use:

```java
@Service
```

for application/service-layer classes.

Example:

```java
@Service
public class OrderService {
}
```

The annotation communicates architectural intent.

It tells developers:

> This class represents service/application logic.

---

# 15. `@Repository`

Repositories represent data access.

Example:

```java
@Repository
public class SomeRepository {
}
```

With Spring Data JPA, repository interfaces can be automatically implemented by Spring.

Example:

```java
public interface OrderRepository
        extends JpaRepository<Order, Long> {
}
```

Spring creates the implementation at runtime.

Conceptually:

```text
OrderRepository interface
          ↓
Spring Data
          ↓
Generated implementation
          ↓
Hibernate/JPA
          ↓
PostgreSQL
```

---

# 16. `@RestController`

A REST controller handles HTTP requests.

Example:

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
}
```

Its responsibilities should include:

* HTTP routing
* request parameters
* request DTOs
* response DTOs
* HTTP status codes
* delegation to services

It should not contain large business rules.

---

# 17. Layered Architecture

Our application follows:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Each layer has a responsibility.

---

## Controller

Responsible for:

```text
HTTP
JSON
Request parameters
Validation entry point
HTTP response
```

Not responsible for:

```text
Complex business rules
Database queries
Transaction orchestration
```

---

## Service

Responsible for:

```text
Use cases
Business orchestration
Business rules
Transaction boundaries
Coordination between repositories
```

---

## Repository

Responsible for:

```text
Database access
Persistence queries
Entity persistence
```

---

## Entity

Responsible for representing persistent domain state.

Example:

```text
Order
Customer
```

---

# 18. Dependency Direction

The dependency direction should normally be:

```text
Controller
    ↓
Service
    ↓
Repository
```

Not:

```text
Repository
    ↓
Controller
```

and ideally not:

```text
Controller
    ↓
Database
```

Direct database access from controllers creates architectural coupling.

---

# 19. Example Request Flow

Suppose the client sends:

```http
GET /api/v1/orders/10
```

The flow is:

```text
Client
  ↓
HTTP Request
  ↓
OrderController
  ↓
OrderService
  ↓
OrderRepository
  ↓
Hibernate/JPA
  ↓
PostgreSQL
```

The response travels back:

```text
PostgreSQL
  ↓
Repository
  ↓
Service
  ↓
Controller
  ↓
JSON
  ↓
Client
```

This is the core backend execution model.

---

# 20. Spring Container Mental Model

Think of the Spring container as an object factory and dependency graph manager.

Conceptually:

```text
Spring Container
│
├── OrderRepository
│
├── CustomerRepository
│
├── OrderService
│      │
│      └── OrderRepository
│
├── CustomerService
│      │
│      └── CustomerRepository
│
└── OrderController
       │
       └── OrderService
```

Spring creates the objects and connects them.

---

# 21. Singleton Scope

By default, Spring beans are singleton-scoped.

That means one bean instance is normally created per Spring application context.

Conceptually:

```text
Spring Container

OrderService
     │
     ├── Controller A
     ├── Controller B
     └── Other consumers
```

They normally reference the same `OrderService` instance.

Therefore service classes should generally be designed to be stateless.

Prefer:

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

Avoid storing request-specific mutable state:

```java
private String currentCustomer;
```

That would create concurrency problems.

---

# 22. Thread Safety

Because Spring singleton beans may be shared across requests:

```text
Request A ─┐
           ├── OrderService
Request B ─┤
           │
Request C ─┘
```

the service should not store request-specific mutable state in instance fields.

Good:

```java
private final OrderRepository repository;
```

Risky:

```java
private Long currentOrderId;
```

The latter can be overwritten by concurrent requests.

This becomes increasingly important as the banking backend becomes concurrent and distributed.

---

# 23. SOLID Connection

Today's topic strongly connects to SOLID.

## Single Responsibility Principle

Controller:

```text
HTTP responsibility
```

Service:

```text
business/application responsibility
```

Repository:

```text
persistence responsibility
```

---

## Dependency Inversion Principle

Higher-level business logic should not be tightly coupled to concrete infrastructure.

For example:

```text
Service
   ↓
Repository abstraction
```

rather than:

```text
Service
   ↓
direct JDBC implementation
```

---

# 24. Dependency Inversion vs Dependency Injection

These are related but different concepts.

### Dependency Inversion Principle

A design principle.

### Dependency Injection

A technique for supplying dependencies.

Example:

```text
DIP
↓
Depend on abstractions

DI
↓
Supply those dependencies externally
```

Spring provides the machinery to perform DI.

---

# 25. Interface-Based Design

Consider:

```java
public interface PaymentGateway {
    void charge();
}
```

Implementation:

```java
@Component
public class BankPaymentGateway
        implements PaymentGateway {

    @Override
    public void charge() {
        // implementation
    }
}
```

Service:

```java
@Service
public class PaymentService {

    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```

The service depends on the abstraction.

This becomes valuable later when the project introduces:

* external banking APIs
* payment gateways
* messaging
* fraud services
* notification services
* multiple implementations
* test doubles

---

# 26. Do Not Over-Abtract

Do not create interfaces everywhere simply because interfaces exist.

Bad:

```text
OrderService
OrderServiceImpl
IOrderService
IOrderServiceImpl
```

when there is only one implementation and no meaningful abstraction.

This violates KISS/YAGNI if the abstraction provides no real benefit.

Prefer:

```java
@Service
public class OrderService {
}
```

until a genuine abstraction is useful.

---

# 27. KISS

**Keep It Simple.**

Spring can support extremely complicated architectures.

That does not mean we should build them immediately.

Today:

```text
Controller
    ↓
Service
    ↓
Repository
```

is enough.

Later, when requirements justify it, we can introduce:

```text
Domain Services
Application Services
Ports
Adapters
Events
Messaging
Microservices
```

Architecture should evolve from requirements.

---

# 28. YAGNI

**You Aren't Gonna Need It.**

Do not add:

```text
10 abstraction layers
20 factories
5 interfaces
multiple modules
microservices
```

before they are needed.

Today's objective is a clean foundation.

---

# 29. High Cohesion

A class should have a focused purpose.

Good:

```text
OrderController
OrderService
OrderRepository
```

Less cohesive:

```text
OrderController
    ├── database SQL
    ├── email sending
    ├── payment processing
    ├── business calculations
    └── HTTP handling
```

High cohesion makes the application easier to maintain.

---

# 30. Low Coupling

Components should depend on as little unnecessary implementation detail as possible.

Prefer:

```text
Controller → Service
```

instead of:

```text
Controller → Hibernate Session → SQL → Database
```

Low coupling makes future changes easier.

---

# 31. Fail Fast

Dependencies should be required during object construction.

Example:

```java
public OrderService(OrderRepository repository) {

    if (repository == null) {
        throw new IllegalArgumentException(
            "repository must not be null"
        );
    }

    this.repository = repository;
}
```

In normal Spring applications, dependency resolution itself should catch missing beans during startup.

This is useful because:

```text
Application starts
        ↓
Dependency missing
        ↓
Startup failure
```

is much better than:

```text
Application starts
        ↓
User sends request
        ↓
NullPointerException
```

---

# 32. Observability Connection

Spring applications should eventually expose:

* health
* metrics
* logs
* traces
* application state

The project already has Actuator dependencies.

We will build this further later.

The principle is:

```text
If something fails in production,
we need evidence.
```

---

# 33. Security Connection

Dependency Injection also affects security architecture.

For example:

```text
Controller
    ↓
Authenticated user
    ↓
Authorization
    ↓
Service
    ↓
Repository
```

Later we will introduce:

* authentication
* authorization
* roles
* permissions
* JWT
* audit logging
* security boundaries

Security should not be bolted on at the very end.

---

# 34. TDD Connection

When creating a service, constructor injection makes unit testing straightforward.

Example:

```java
OrderRepository repository =
        mock(OrderRepository.class);

OrderService service =
        new OrderService(repository);
```

Then test:

```text
Given repository
When service executes
Then expected result is returned
```

This supports:

```text
Red
 ↓
Green
 ↓
Refactor
```

without requiring the entire Spring application to start.

---

# 35. DDD Connection

The application will eventually become more domain-oriented.

Current:

```text
Order
Customer
```

Later:

```text
Customer
Account
Order
Payment
Transaction
Ledger
Transfer
Beneficiary
RiskAssessment
AuditRecord
```

DDD will help us define:

* entities
* value objects
* aggregates
* domain services
* domain events
* bounded contexts

Spring Dependency Injection is infrastructure supporting these concepts, not the domain model itself.

---

# 36. Contract-First Connection

The API contract should remain independent from implementation details.

Conceptually:

```text
API Contract
     ↓
Controller
     ↓
Service
     ↓
Repository
```

Later we will formalize this with OpenAPI.

The external API should not change merely because the internal implementation changes.

---

# 37. Event-Driven Connection

Later the project may evolve from:

```text
OrderService
    ↓
Database
```

to:

```text
OrderService
    ↓
OrderCreated
    ↓
Event Infrastructure
    ├── Notification
    ├── Audit
    ├── Analytics
    └── Other bounded context
```

But events should be introduced only when there is a genuine business/system requirement.

---

# 38. Reliability-Driven Connection

Dependency injection makes dependencies explicit.

That allows us later to introduce:

```text
ExternalPaymentClient
        ↓
Timeout
        ↓
Retry
        ↓
Circuit Breaker
        ↓
Fallback
```

Each dependency becomes a clearly identifiable failure boundary.

---

# 39. DevOps-Driven Connection

The application lifecycle is:

```text
Code
 ↓
Git
 ↓
Build
 ↓
Test
 ↓
Package
 ↓
Container
 ↓
CI/CD
 ↓
Deployment
 ↓
Monitoring
```

Spring's dependency graph is part of what must be validated during build and startup.

A production application is not complete merely because it compiles.

---

# 40. Windows Environment Verification

Open **PowerShell**.

### Step 1 — Open PowerShell

Click:

```text
Start
```

Then type:

```text
PowerShell
```

Click:

```text
Windows PowerShell
```

---

### Step 2 — Navigate to the project

Run:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Verify:

```powershell
Get-Location
```

Expected:

```text
Path
----
C:\Users\User\Documents\JavaProjects\enterprise-order-api
```

---

# 41. Check Git Status

Run:

```powershell
git status
```

Then:

```powershell
git log --oneline -8
```

Confirm you are on:

```text
main
```

---

# 42. Inspect Project Structure

Run:

```powershell
Get-ChildItem
```

Then:

```powershell
Get-ChildItem .\src\main\java\com\faris\enterprise_order_api -Recurse
```

You should see the existing application structure.

---

# 43. IntelliJ IDEA — Open Project

If using IntelliJ IDEA:

### Step 1

Open IntelliJ IDEA.

### Step 2

Click:

```text
File
```

### Step 3

Click:

```text
Open
```

### Step 4

Select:

```text
C:\Users\User\Documents\JavaProjects\enterprise-order-api
```

### Step 5

Click:

```text
OK
```

Wait for Maven indexing to finish.

---

# 44. Verify Main Application Class

In IntelliJ:

```text
Project
 └── src
      └── main
           └── java
                └── com.faris.enterprise_order_api
```

Open:

```text
EnterpriseOrderApiApplication.java
```

Confirm it contains:

```java
@SpringBootApplication
```

---

# 45. Verify Service Components

Navigate to:

```text
src
└── main
    └── java
        └── com.faris.enterprise_order_api
            └── service
```

Inspect the service classes.

Confirm service classes use:

```java
@Service
```

and dependencies are injected through constructors.

---

# 46. Verify Controllers

Navigate to:

```text
controller
```

Confirm REST controllers use:

```java
@RestController
```

and:

```java
@RequestMapping(...)
```

The controller should delegate to services rather than directly performing database operations.

---

# 47. Verify Repositories

Navigate to:

```text
repository
```

Confirm repositories are Spring Data interfaces.

For example:

```java
public interface OrderRepository
        extends JpaRepository<Order, Long> {
}
```

Spring Data creates the implementation.

---

# 48. Run the Application

From PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Watch the startup log.

You should see Spring Boot starting.

Do not close the terminal while the application is running.

---

# 49. What Happens During Startup?

Conceptually:

```text
main()
  ↓
SpringApplication.run()
  ↓
Create ApplicationContext
  ↓
Scan components
  ↓
Create beans
  ↓
Resolve dependencies
  ↓
Create repositories
  ↓
Create services
  ↓
Create controllers
  ↓
Start embedded web server
  ↓
Application ready
```

This is one of the most important Spring concepts to understand.

---

# 50. Startup Failure Example

Imagine:

```java
@Service
public class OrderService {

    public OrderService(
        MissingDependency dependency
    ) {
    }
}
```

If Spring cannot find a bean for:

```text
MissingDependency
```

the application should fail during startup.

Conceptually:

```text
Application
    ↓
Dependency resolution
    ↓
Missing bean
    ↓
Startup failure
```

This is Fail Fast.

---

# 51. Test the Existing API

With the application running, open another PowerShell window.

Navigate to the project:

```powershell
cd "C:\Users\User\Documents\JavaProjects\enterprise-order-api"
```

Test:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/orders" `
    -Method GET
```

If the endpoint exists and the application/database are configured correctly, the API should respond.

---

# 52. Run Tests

Stop the application if necessary.

Run:

```powershell
.\mvnw.cmd clean test
```

Expected:

```text
BUILD SUCCESS
```

The exact test count may change as the project evolves.

The important conditions are:

```text
Failures = 0
Errors = 0
```

---

# 53. Architecture Verification Checklist

Check each item.

```text
[ ] Controller handles HTTP concerns
[ ] Controller delegates to Service
[ ] Service contains application/business orchestration
[ ] Repository handles persistence
[ ] Dependencies are injected
[ ] Constructor injection is preferred
[ ] Required dependencies are final
[ ] No unnecessary field injection
[ ] Spring components are discoverable
[ ] Application starts successfully
[ ] Existing API still works
[ ] Tests pass
```

---

# 54. Engineering Principle Review

## SOLID

```text
S — Single Responsibility
O — Open/Closed
L — Liskov Substitution
I — Interface Segregation
D — Dependency Inversion
```

Today's strongest connection:

```text
Dependency Inversion
        +
Dependency Injection
```

---

## DRY

Do not duplicate object construction or configuration unnecessarily.

Spring manages common infrastructure.

---

## KISS

Use Spring's conventions instead of unnecessary custom infrastructure.

---

## YAGNI

Do not create abstractions without a reason.

---

## Separation of Concerns

Keep:

```text
HTTP
Business Logic
Persistence
```

separate.

---

## High Cohesion

Each class should have a focused responsibility.

---

## Low Coupling

Depend on abstractions and clearly defined layers.

---

## Encapsulation

Keep implementation details inside their appropriate components.

---

## Composition

Prefer composing objects through dependencies rather than large inheritance hierarchies.

---

## Fail Fast

Missing dependencies should be detected during startup.

---

## Least Privilege

Components should only have the dependencies/access they actually need.

---

## Design for Failure

Every dependency can eventually fail.

Later:

```text
Database
External API
Redis
Kafka
RabbitMQ
```

will all become potential failure boundaries.

---

## Idempotency

Dependency injection itself does not provide idempotency.

Business operations must explicitly design for it.

---

## Observability

A Spring component should eventually be observable through:

```text
Logs
Metrics
Traces
Health
```

---

## Measure First

Do not introduce architecture simply because it sounds enterprise.

Measure and understand the actual problem.

---

# 55. Common Mistakes

## Mistake 1 — Field Injection

Avoid:

```java
@Autowired
private OrderRepository repository;
```

Prefer constructor injection.

---

## Mistake 2 — Business Logic in Controller

Avoid:

```java
@PostMapping
public ResponseEntity<?> create(...) {

    // 100 lines of business logic
}
```

Prefer:

```text
Controller
    ↓
Service
```

---

## Mistake 3 — Database Logic in Controller

Avoid:

```java
controller
    ↓
repository
```

when business/application orchestration is required.

Prefer:

```text
controller
    ↓
service
    ↓
repository
```

---

## Mistake 4 — Excessive Interfaces

Do not automatically create:

```text
OrderService
OrderServiceImpl
IOrderService
```

without a real reason.

---

## Mistake 5 — Stateful Singleton Services

Avoid:

```java
private Long currentUserId;
```

inside singleton services.

---

## Mistake 6 — Static Global State

Avoid:

```java
public static SomeObject global;
```

as a substitute for Dependency Injection.

---

## Mistake 7 — Circular Dependencies

Avoid:

```text
Service A
   ↓
Service B
   ↓
Service A
```

Circular dependencies usually indicate a design problem.

---

# 56. Day 3 Mental Model

Remember this:

```text
Spring Boot
     ↓
Spring Application Context
     ↓
Creates Beans
     ↓
Resolves Dependencies
     ↓
Injects Dependencies
     ↓
Starts Application
```

And:

```text
HTTP
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Database
```

The controller should not need to know how the database works.

The service should not need to know HTTP details.

The repository should not decide HTTP status codes.

That separation is the foundation of maintainable enterprise backend systems.

---

# 57. Project Evolution

Today's contribution to the single project is architectural understanding and verification.

The project now has a clearer dependency graph:

```text
                    Spring Container
                          │
          ┌───────────────┼───────────────┐
          ↓               ↓               ↓
    Controller         Service        Repository
          │               │               │
          └───────────────┘               │
                  │                       │
                  └───────────────────────┘
                          │
                       Database
```

This architecture will support future additions such as:

```text
Validation
Transactions
Security
Caching
Messaging
Events
Audit
Observability
Microservices
Docker
CI/CD
Kubernetes
Cloud
AI integration
```

without abandoning the same application.

---

# 58. Git Workflow

Before committing:

```powershell
git status
```

Review changed files:

```powershell
git diff --stat
```

Then:

```powershell
git diff
```

Stage:

```powershell
git add .
```

Review staged changes:

```powershell
git diff --cached --stat
```

Then:

```powershell
git diff --cached
```

Commit:

```powershell
git commit -m "docs(day-03): document Spring dependency injection architecture"
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

# 59. Day 3 Definition of Done

Day 3 is complete when:

```text
[ ] Understand IoC
[ ] Understand Dependency Injection
[ ] Understand Spring Beans
[ ] Understand component scanning
[ ] Understand @SpringBootApplication
[ ] Understand @Component
[ ] Understand @Service
[ ] Understand @Repository
[ ] Understand @RestController
[ ] Understand constructor injection
[ ] Understand singleton scope
[ ] Understand stateless services
[ ] Understand dependency direction
[ ] Understand layered architecture
[ ] Understand SOLID connection
[ ] Verify project structure
[ ] Application starts
[ ] Existing API works
[ ] Tests pass
[ ] Day 3 documentation saved
[ ] Git commit created
[ ] Git push completed
[ ] Working tree clean
```

---

# 60. Notebook Summary

## Day 3 — Spring IoC & Dependency Injection

### Core concepts

```text
IoC
= Spring controls object creation/lifecycle.

DI
= Dependencies are supplied to a class.

Bean
= Object managed by Spring.

Component Scanning
= Spring discovers components automatically.

Constructor Injection
= Required dependencies supplied through constructor.
```

### Main annotations

```text
@SpringBootApplication
@Component
@Service
@Repository
@RestController
```

### Preferred dependency style

```java
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

### Architecture

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Important principles

```text
SOLID
DRY
KISS
YAGNI
SoC
High Cohesion
Low Coupling
Encapsulation
Composition
Fail Fast
Least Privilege
Design for Failure
Idempotency
Observability
Measure First
```

### Key rule

> **Classes should declare what they need; Spring should provide those dependencies.**

### Most important mental model

```text
Spring Container
       ↓
Creates Beans
       ↓
Resolves Dependencies
       ↓
Injects Dependencies
       ↓
Application Runs
```

### Git

```powershell
git add .
git commit -m "docs(day-03): document Spring dependency injection architecture"
git push origin main
```

---

# 61. Day 3 Final Takeaway

A professional Spring Boot backend is not simply:

```text
Controller + Service + Repository
```

The deeper concept is:

```text
Well-defined responsibilities
        +
Explicit dependencies
        +
Dependency Injection
        +
Low coupling
        +
High cohesion
        +
Testability
        +
Fail-fast startup
```

Spring provides the infrastructure for connecting the application.

**The developer remains responsible for designing the dependency graph correctly.**

That distinction becomes increasingly important as `enterprise-order-api` grows from a simple REST API into an enterprise banking backend.
