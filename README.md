# Bank Microservices

A domain-driven microservices system for managing bank customers and accounts, built with Spring Boot 4.1.1, Java 17,
Apache Kafka, PostgreSQL, and Netflix Eureka. The system features a **service discovery architecture** with Eureka,
where multiple microservices communicate asynchronously via the **Transactional Outbox pattern** and
**Event-Driven Architecture (EDA)**.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Service Discovery with Eurka](#service-discovery-with-eurka)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Authentication & Security](#authentication--security)
- [Event System](#event-system)
- [Business Rules](#business-rules)
- [Testing](#testing)
- [Design Decisions](#design-decisions)
- [Shortcomings](#shortcomings)
- [Assumptions](#assumptions)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                         CLIENT                                  │
└──────────────┬─────────────────────────────┬─────────────────────┘
               │ REST                        │ REST
               v                             v
┌──────────────────────────┐   ┌──────────────────────────┐
│    customer-service      │   │     account-service       │
│       (Port 8001)        │   │       (Port 8002)         │
│                          │   │                           │
│  ┌────────────────────┐  │   │  ┌─────────────────────┐  │
│  │   AuthController   │  │   │  │  AccountsController │  │
│  │  CustomerController│  │   │  └─────────────────────┘  │
│  └────────────────────┘  │   │                           │
│                          │   │  ┌─────────────────────┐  │
│  ┌────────────────────┐  │   │  │  Kafka Consumer     │  │
│  │  Outbox Publisher  │──┼──►│  │  (USER_CUSTOMER)    │  │
│  │  (every 2s)        │  │   │  └─────────────────────┘  │
│  └────────────────────┘  │   │                           │
│                          │   │  ┌─────────────────────┐  │
│  ┌────────────────────┐  │   │  │  AccountService     │  │
│  │  EventService      │  │   │  └─────────────────────┘  │
│  └────────────────────┘  │   │                           │
└──────────────┬───────────┘   └──────────────┬────────────┘
               │                              │
               │ Registers to Eureka          │ Registers to Eureka
               │                              │
               v                              v
┌──────────────────────────────────────────────────────────┐
│         Netflix Eureka Server (Port 8761)               │
│     Service Discovery & Service Registry                │
│  - Customer Service Registration                        │
│  - Account Service Registration                         │
│  - Health Checks & Instance Status                      │
└──────────────┬─────────────────────────────┬────────────┘
               │                             │
               v                             v
┌──────────────────────────┐   ┌──────────────────────────┐
│  PostgreSQL (5432)       │   │  PostgreSQL (5433)       │
│  database-customers      │   │  database-accounts       │
└──────────────────────────┘   └──────────────────────────┘

               ┌──────────────────────────┐
               │    Apache Kafka (9092)   │
               │  Topic: USER_CUSTOMER    │
               │  KRaft mode (no ZK)      │
               └──────────────────────────┘
```

**Communication flow:**

1. Client registers a customer via `customer-service`
2. A `USER_CUSTOMER_CREATED` event is saved to the outbox table (PENDING)
3. A scheduled publisher polls every 2 seconds and sends events to Kafka topic `USER_CUSTOMER`
4. `account-service` consumes the event and auto-creates a SAVINGS account
5. Status changes and deletions follow the same pattern

---

## Tech Stack

| Category          | Technology                   | Version                  |
| ----------------- | ---------------------------- | ------------------------ |
| Language          | Java                         | 17                       |
| Framework         | Spring Boot                  | 4.1.1                    |
| Service Discovery | Netflix Eureka               | (Spring Cloud managed)   |
| Cloud Framework   | Spring Cloud                 | 2023.0.x                 |
| Build Tool        | Maven                        | 3.9.6                    |
| Database          | PostgreSQL                   | 18.3                     |
| ORM               | Spring Data JPA + Hibernate  | (managed by Spring Boot) |
| Messaging         | Apache Kafka                 | 4.3.1 (KRaft)            |
| Security          | Spring Security + JWT (JJWT) | 0.12.6                   |
| API Docs          | SpringDoc OpenAPI            | 2.7.0                    |
| Serialization     | Jackson                      | 2.22.1                   |
| Code Gen          | Lombok                       | (managed)                |
| Testing           | JUnit 5, Mockito, H2, JaCoCo | 0.8.12 (JaCoCo)          |
| Containerization  | Docker + Docker Compose      | Multi-stage builds       |
| Monitoring        | Spring Boot Actuator         | (managed)                |

---

## Project Structure

```
bank-microservices/
├── docker-compose.yml              # Orchestrates all services
├── .env.example                    # Environment variable template
├── postman_collection.json         # Pre-configured API test collection
├── qodana.yaml                     # Static analysis config
│
├── eurka-server/                   # Eureka Service Discovery (Port 8761)
│   ├── pom.xml
│   ├── HELP.md
│   └── src/main/java/com/neo/eurkaserver/
│       ├── EurkaServerApplication.java   # @EnableEurekaServer
│       └── resources/
│           └── application.yml
│
├── customer-service/               # Customer management (Port 8001)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/neo/customerservice/
│       ├── controller/             # REST endpoints
│       │   ├── AuthController.java
│       │   └── CustomerController.java
│       ├── services/
│       │   ├── auth/               # Registration & login
│       │   ├── user/               # User CRUD + business logic
│       │   ├── jwt/                # JWT token generation/validation
│       │   ├── event/              # Outbox event persistence
│       │   └── outbox/             # Kafka publisher (polling)
│       ├── entity/                 # JPA entities (User, Address, Event)
│       ├── dto/                    # Request/response DTOs + events
│       ├── enums/                  # UserRoles, EventType, etc.
│       ├── filter/                 # JwtAuthFilter
│       ├── config/                 # Security, Kafka, OpenAPI, Seeder
│       ├── repository/             # Spring Data JPA repos
│       └── exceptions/             # Custom exceptions + handler
│
├── account-service/                # Account management (Port 8002)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/neo/accountservice/
│       ├── controllers/
│       │   └── AccountsController.java
│       ├── services/
│       │   ├── account/            # Account CRUD + business rules
│       │   └── consumer/           # Kafka event consumer
│       ├── entity/                 # Account, CustomerStatus, Event
│       ├── dto/                    # Request/response DTOs + events
│       ├── enums/                  # AccountType, AccountStatus, etc.
│       ├── config/                 # Security (OAuth2 RS), JWT, OpenAPI
│       ├── repository/             # Spring Data JPA repos
│       └── exceptions/             # Custom exceptions + handler
```

````

---

## Prerequisites

- **Java 17+**
- **Docker & Docker Compose**
- **Maven** (or use the included `mvnw` wrapper)

---

## Getting Started

### 1. Clone and configure

```bash
git clone <repository-url>
cd bank-microservices
cp .env.example .env
````

### 2. Run with Docker Compose

```bash
docker-compose up --build
```

This starts:
| Service | URL |
|---|---|
| eurka-server (Eureka) | `http://localhost:8761` |
| customer-service | `http://localhost:8001` |
| account-service | `http://localhost:8002` |
| PostgreSQL (customers) | `localhost:5432` |
| PostgreSQL (accounts) | `localhost:5433` |
| Kafka | `localhost:9092` |

**Note**: Ensure the Eureka Server starts first so that other services can register with it.

### 3. Run locally (without Docker)

Start PostgreSQL, Kafka, and Eureka Server manually, then:

```bash
# Terminal 1 - Eureka Server
cd eurka-server
mvn spring-boot:run

# Terminal 2 - Customer Service
cd customer-service
mvn spring-boot:run

# Terminal 3 - Account Service
cd account-service
mvn spring-boot:run
```

After starting all services, check the Eureka dashboard at `http://localhost:8761` to see registered instances.

### 4. Default Admin Account

On startup, an admin user is seeded:

| Field    | Value   |
| -------- | ------- |
| username | `admin` |
| password | `admin` |

### 5. Environment Variables

| Variable            | Default    | Description                     |
| ------------------- | ---------- | ------------------------------- |
| `POSTGRES_USER`     | `postgres` | PostgreSQL username             |
| `POSTGRES_PASSWORD` | `postgres` | PostgreSQL password             |
| `JWT_SECRET`        | `secret`   | Shared JWT signing key (Base64) |
| `JWT_EXPIRATION`    | `3600000`  | JWT token expiry in ms          |

---

## API Reference

### customer-service (Port 8001)

#### Authentication

| Method | Endpoint                | Description             | Auth   |
| ------ | ----------------------- | ----------------------- | ------ |
| `POST` | `/api/v1/auth/register` | Register a new customer | Public |
| `POST` | `/api/v1/auth/login`    | Login and receive JWT   | Public |

#### Customers

| Method   | Endpoint                        | Description                | Auth            |
| -------- | ------------------------------- | -------------------------- | --------------- |
| `GET`    | `/api/v1/customers`             | List customers (paginated) | ADMIN, CUSTOMER |
| `GET`    | `/api/v1/customers/{id}`        | Get customer by ID         | ADMIN, CUSTOMER |
| `PUT`    | `/api/v1/customers`             | Update own profile         | ADMIN, CUSTOMER |
| `PUT`    | `/api/v1/customers/{id}/status` | Enable/disable customer    | ADMIN only      |
| `DELETE` | `/api/v1/customers/{id}`        | Delete a customer          | ADMIN only      |

### account-service (Port 8002)

#### Accounts

| Method   | Endpoint                                 | Description                   | Auth            |
| -------- | ---------------------------------------- | ----------------------------- | --------------- |
| `POST`   | `/api/v1/accounts`                       | Create an account             | ADMIN, CUSTOMER |
| `GET`    | `/api/v1/accounts`                       | List all accounts (paginated) | ADMIN only      |
| `GET`    | `/api/v1/accounts/{id}`                  | Get account by ID             | ADMIN, CUSTOMER |
| `GET`    | `/api/v1/accounts/customer/{customerId}` | Get accounts by customer      | ADMIN only      |
| `PUT`    | `/api/v1/accounts/{id}`                  | Update account                | ADMIN only      |
| `DELETE` | `/api/v1/accounts/{id}`                  | Delete account                | ADMIN only      |

### Swagger UI

| Service          | URL                                           |
| ---------------- | --------------------------------------------- |
| eurka-server     | `http://localhost:8761` (Eureka Dashboard)    |
| customer-service | `http://localhost:8001/swagger-ui/index.html` |
| account-service  | `http://localhost:8002/swagger-ui/index.html` |

---

## Authentication & Security

### JWT Flow

```
Register -> Login -> Receive JWT -> Use as Bearer token
```

**Token payload:**

```json
{
  "sub": "username",
  "role": "ROLE_CUSTOMER",
  "userId": 1000001,
  "iat": 1694000000,
  "exp": 1694000300
}
```

### Security Architecture

| Aspect           | customer-service                              | account-service                        |
| ---------------- | --------------------------------------------- | -------------------------------------- |
| Mechanism        | Custom `JwtAuthFilter` (OncePerRequestFilter) | Spring Security OAuth2 Resource Server |
| JWT Library      | JJWT (0.12.6)                                 | Spring `NimbusJwtDecoder`              |
| Public Endpoints | `/api/v1/auth/**`, Swagger                    | Swagger only                           |
| Session          | Stateless                                     | Stateless                              |
| Role Extraction  | From `UserDetails.getAuthorities()`           | From JWT `"role"` claim                |

### Role-Based Access

| Role       | Permissions                                                 |
| ---------- | ----------------------------------------------------------- |
| `ADMIN`    | Full access to all endpoints                                |
| `CUSTOMER` | View/update own profile, create accounts, view own accounts |

Both services share the same JWT secret key, so tokens issued by `customer-service` are validated by `account-service`.

---

## Event System

### Outbox Pattern

Instead of publishing directly to Kafka (which risks data loss on failure), the customer-service uses the
**Transactional Outbox pattern**:

1. Domain events are saved to an `events` table within the same transaction as business data
2. A scheduled task polls PENDING events every 2 seconds
3. Events are published to Kafka using `FOR UPDATE SKIP LOCKED` for safe concurrent processing
4. On success, events are marked as PROCESSED
5. On failure, events remain PENDING for retry

### Event Types

| Event                          | Trigger               | Account-Service Action                 |
| ------------------------------ | --------------------- | -------------------------------------- |
| `USER_CUSTOMER_CREATED`        | New user registered   | Auto-creates a SAVINGS account         |
| `USER_CUSTOMER_DELETED`        | User deleted          | Deletes all accounts for that customer |
| `USER_CUSTOMER_STATUS_CHANGED` | User enabled/disabled | Suspends or reactivates all accounts   |

### Kafka Configuration

| Property               | Value                    |
| ---------------------- | ------------------------ |
| Topic                  | `USER_CUSTOMER`          |
| Customer-service group | `customer-service-group` |
| Account-service group  | `accounts-service-group` |
| Mode                   | KRaft (no ZooKeeper)     |
| Auto-create topics     | `true`                   |

---

## Business Rules

### Customer Rules

- Customer ID is auto-generated starting at `1,000,000` (7 digits)
- Password must be 8-30 characters
- Email must be unique
- Username must be unique

### Account Rules

| Rule                      | Detail                                                              |
| ------------------------- | ------------------------------------------------------------------- |
| Account number            | 10 digits: `customerId * 1000 + sequenceNumber`                     |
| Max accounts per customer | 9 (sequence 1-9)                                                    |
| Salary account limit      | Only 1 SALARY account per customer                                  |
| Account types             | SALARY, SAVINGS, INVESTMENT                                         |
| Default type              | SAVINGS (when null)                                                 |
| Default balance           | 0.00                                                                |
| Default status            | ACTIVE                                                              |
| Auto-creation             | New customers automatically get a SAVINGS account                   |
| Concurrency               | Pessimistic locking on account counter for safe sequence generation |

### Account Number Examples

| Customer ID | Sequence | Account Number     |
| ----------- | -------- | ------------------ |
| 1000001     | 1        | `1000001001`       |
| 1000001     | 2        | `1000001002`       |
| 1000001     | 9        | `1000001009` (max) |

---

## Testing

### Run Tests

```bash
# Customer service
cd customer-service
mvn test

# Account service
cd account-service
mvn test
```

### Test Coverage

JaCoCo is configured in both services for code coverage reporting:

```bash
# Generate coverage report
mvn test jacoco:report

# View report
open target/site/jacoco/index.html
```

### Test Summary

| Service          | Test Classes | Approach                                              |
| ---------------- | ------------ | ----------------------------------------------------- |
| customer-service | 20+          | `@WebMvcTest`, `@DataJpaTest`, Mockito unit tests     |
| account-service  | 7+           | Mockito unit tests, `@DataJpaTest`, `@SpringBootTest` |

### Test Types

- **Controller tests**: `@WebMvcTest` with `MockMvc` for HTTP layer testing
- **Repository tests**: `@DataJpaTest` with H2 in-memory database
- **Service tests**: Mockito with `@ExtendWith(MockitoExtension.class)`
- **DTO validation**: Bean Validation constraint tests
- **JWT tests**: Token generation, parsing, expiration, and signing

### Postman Collection

A pre-configured Postman collection (`postman_collection.json`) is included with 11 requests and automated test scripts
covering the full registration-to-account-creation flow.

---

## Design Decisions

### 1. Event-Driven Architecture over Inter-Service REST Calls

An alternative approach would be for `account-service` to call `customer-service` via synchronous REST to fetch user
data on demand. While simpler at first glance, this approach introduces significant problems in a microservices
environment:

- **Security overhead**: Each inter-service REST call must be secured — requiring mutual TLS, API keys, or a
  service-to-service JWT issuance flow. Every new service added to the mesh multiplies this complexity.
- **Synchronous coupling**: If `customer-service` is down, slow, or restarted, `account-service` requests block or fail.
  This creates a cascading failure chain across the entire system.
- **Single point of failure**: The request path becomes `Client -> account-service -> customer-service -> DB`, meaning
  two services and two networks must all be available for a single operation.
- **Latency**: Each inter-service hop adds network latency. A single user lookup could involve two HTTP round trips
  plus two TLS handshakes.

By using Kafka for asynchronous communication and maintaining a local projection of customer status in `account-service`,
the system achieves loose coupling, resilience under partial failure, and independent deployability. `account-service`
never depends on `customer-service` being online at runtime.

### 2. The Outbox Pattern — Guaranteed Event Delivery

The outbox pattern solves a fundamental distributed systems problem: **how to atomically update business data and publish
an event to a message broker** when the database and Kafka are two separate systems.

#### The Problem

Without the outbox pattern, a naive approach would be:

```
BEGIN TRANSACTION
  INSERT INTO users (...)
  -- Kafka publish happens here, but what if it fails?
  KAFKA_PUBLISH("USER_CUSTOMER_CREATED", ...)
COMMIT
```

If the Kafka publish fails after the INSERT, the transaction is rolled back and no event is sent — but the business
data was never persisted. If the Kafka publish succeeds but the commit fails, the event was sent but the data was never
saved. There is no way to make both operations atomic across two different systems.

#### How the Outbox Pattern Solves It

The outbox pattern keeps both writes in the **same database transaction**, then publishes asynchronously:

```
BEGIN TRANSACTION
  INSERT INTO users (...)              -- Business data
  INSERT INTO events (status=PENDING)  -- Outbox event (same DB, same TX)
COMMIT
```

Since both writes hit the same PostgreSQL database, they are guaranteed to be atomic — either both succeed or both fail.
No distributed transaction coordinator is needed.

#### The Publishing Lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│  1. WRITE  (synchronous, within business TX)               │
│     User saved + Event saved with status = PENDING         │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           v
┌─────────────────────────────────────────────────────────────┐
│  2. POLL  (every 2 seconds, @Scheduled)                    │
│     SELECT * FROM events                                   │
│     WHERE status = 'PENDING'                               │
│     ORDER BY created_date ASC                              │
│     LIMIT 100                                              │
│     FOR UPDATE SKIP LOCKED                                 │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           v
┌─────────────────────────────────────────────────────────────┐
│  3. PUBLISH (to Kafka)                                     │
│     kafkaTemplate.send("USER_CUSTOMER", event)             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                    ┌──────┴──────┐
                    │  Success?   │
                    └──────┬──────┘
                   Yes     │     No
                    │      │      │
                    v      │      v
              ┌─────────┐  │  ┌──────────────┐
              │PROCESSED│  │  │  Stay PENDING │
              └─────────┘  │  │  (retry next  │
                           │  │   cycle)      │
                           │  └──────────────┘
```

#### Why `FOR UPDATE SKIP LOCKED`?

This PostgreSQL locking clause is critical for the outbox pattern:

- `FOR UPDATE` locks the selected rows so no other consumer can pick them up simultaneously
- `SKIP LOCKED` tells the query to skip any rows already locked by another consumer
- This allows multiple instances of the publisher to run concurrently without processing the same event twice
- Without it, two publisher instances could both read the same PENDING event and publish duplicates

#### Trade-offs

- Events may be published slightly out of order if the polling interval is high (2s in this case)
- Duplicate delivery is possible if the publisher crashes after Kafka publish but before marking PROCESSED — the
  consumer must be **idempotent** (which the account-service is, since creating an account for an already-existing
  customer is handled gracefully)
- The events table grows indefinitely in the current implementation — a production system should include a cleanup job
  to delete PROCESSED events older than a threshold

### 3. JWT Invalidation, User Deletion, and Redis

#### The Problem

JSON Web Tokens are **stateless** by design — once issued, a token is valid until it expires, regardless of what happens
to the user on the backend. This creates a critical gap:

- A user is **deleted** → their JWT remains valid for up to 5 minutes
- A user is **suspended/disabled** → their JWT still grants access
- An admin **revokes access** → the token still works until natural expiry

In the current implementation, when `customer-service` deletes a user or changes their status, the `account-service`
receives the event and updates its local data. However, if a client still holds a valid JWT for that deleted user, they
can continue making authenticated requests to `account-service` until the token expires. This is a known security gap.

#### The Redis-Based Solution

A Redis-backed token revocation list solves this problem:

```
┌──────────────────────────────────────────────────────────────┐
│  User deleted or status changed                             │
│         │                                                   │
│         v                                                   │
│  customer-service:                                          │
│    1. Delete user from DB                                   │
│    2. Save JWT jti (token ID) to Redis                      │
│       SET revoked:{jti} "deleted" EX 300                    │
│       (TTL = token expiration, auto-expires from Redis)     │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           v
┌──────────────────────────────────────────────────────────────┐
│  Any authenticated request:                                 │
│    1. Parse JWT, extract jti                                │
│    2. Check: EXISTS revoked:{jti} in Redis                  │
│    3. If exists -> reject (401 Unauthorized)                │
│    4. If not found -> proceed normally                      │
└──────────────────────────────────────────────────────────────┘
```

**Why Redis:**

| Aspect       | Why Redis Fits                                                         |
| ------------ | ---------------------------------------------------------------------- |
| Speed        | In-memory lookups add <1ms latency to auth checks                      |
| TTL          | Built-in key expiration — revoked tokens auto-clean after token expiry |
| Shared state | Both services read from the same Redis instance                        |
| Simplicity   | `SET key value EX seconds` is the entire API needed                    |
| Persistence  | Can be configured with AOF/RDB for durability if needed                |

**Implementation would require:**

- A Redis instance added to `docker-compose.yml`
- Spring Data Redis dependency in both services
- A `TokenRevocationService` that stores/retrieves revoked JWT IDs
- The `JwtAuthFilter` (customer-service) and `JwtConfiguration` (account-service) to check Redis before accepting a token

### 4. Keycloak — Recommended for Production and MVP

While the current custom JWT implementation works for demonstrating the concepts, **Keycloak** is the recommended
approach for any production system or even a quick MVP:

#### What Keycloak Provides Out of the Box

- **OAuth2 / OpenID Connect authorization server** — no custom `JwtServiceImpl` needed
- **User registration and login** — built-in UI and REST APIs for auth flows
- **Token issuance and refresh** — access tokens, refresh tokens, ID tokens
- **Token revocation** — native support via the OAuth2 Revocation endpoint (solves the JWT invalidation problem
  described above without Redis)
- **Role-based access control** — realm roles, client roles, role mappings
- **User federation** — connect to LDAP or Active Directory
- **Multi-tenancy** — realm-based isolation
- **Admin console** — web UI for managing users, roles, clients, and tokens

#### How It Simplifies the Architecture

```
Current (custom JWT):
  customer-service  --> JwtServiceImpl (custom JJWT) --> issues tokens
  account-service   --> JwtConfiguration (NimbusJwtDecoder) --> validates tokens
  Both services     --> share a secret key

With Keycloak:
  Keycloak          --> issues and manages all tokens
  customer-service  --> Resource Server (validates from Keycloak)
  account-service   --> Resource Server (validates from Keycloak)
  Both services     --> point to Keycloak's JWKS endpoint (no shared secret)
```

This eliminates:

- `JwtServiceImpl` and all custom token generation logic
- `JwtAuthFilter` in customer-service (Keycloak handles authentication)
- The shared secret configuration between services
- The need for a Redis-based revocation list (Keycloak handles it natively)
- The `AdminSeeder` (users are managed in Keycloak)

#### Docker Integration

Keycloak is available as a Docker image and can be added to `docker-compose.yml` with minimal configuration:

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:latest
  command: start-dev
  ports:
    - "8080:8080"
  environment:
    KEYCLOAK_ADMIN: admin
    KEYCLOAK_ADMIN_PASSWORD: admin
```

Both services would then configure their `SecurityFilterChain` to validate tokens from Keycloak's OIDC discovery URL
(`http://keycloak:8080/realms/master/.well-known/openid-configuration`), making them pure OAuth2 Resource Servers.

### 5. Database-per-Service

Each service owns its own PostgreSQL instance, enforcing service boundaries and independent data management. The
account-service maintains a local projection of customer status rather than querying customer-service directly.

### 6. Dual Security Mechanisms

- **customer-service** uses a custom `JwtAuthFilter` (OncePerRequestFilter) because it issues tokens and manages
  authentication
- **account-service** uses Spring Security's OAuth2 Resource Server because it only validates tokens, making it cleaner
  and more standards-compliant

### 7. Pessimistic Locking for Account Numbers

Account sequence numbers use `PESSIMISTIC_WRITE` locking to prevent race conditions when multiple requests try to create
accounts for the same customer simultaneously.

### 8. Sequence-Based Customer IDs

Customer IDs start at 1,000,000 using a database sequence, ensuring 7-digit IDs as required.

### 9. Spring Profiles

Two profiles are configured:

- `default` (local development): connects to `localhost` databases
- `docker`: connects to Docker service names (`database-customers`, `database-accounts`, `kafka`)

### 10. @EnableMethodSecurity

Both services use `@EnableMethodSecurity` with `@PreAuthorize` annotations on controllers, enabling fine-grained
role-based access control at the method level.

### 11. Netflix Eureka for Service Discovery

Instead of hardcoding service endpoints (e.g., `http://customer-service:8001`), the system uses **Netflix Eureka**
for dynamic service discovery:

- Services self-register with Eureka on startup
- Services can discover each other by querying the Eureka registry

---

## Shortcomings

1. **`create-drop` DDL strategy**: Hibernate is configured with `ddl-auto: create-drop`, meaning the database schema is
   recreated on every startup. This is not suitable for production. A migration tool like Flyway or Liquibase should be
   used.

2. **Hardcoded Kafka bootstrap servers**: The `KafkaConfiguration` in customer-service has `localhost:9092` hardcoded
   rather than reading from `application.yml`. Docker deployments rely on the environment variable override.

3. **Shared JWT secret**: Both services use the same hardcoded Base64 secret key. In production, each service should use
   its own key pair or a centralized JWT issuer.

4. **`getAccountsByCustomerId` bug**: The `AccountServiceImpl.getAccountsByCustomerId()` method returns all accounts via
   `findAll(pageable)` instead of filtering by `customerId`.

5. **No Spring profiles for test environment**: Tests use H2 but there is no dedicated `application-test.yml` profile;
   test configs are in `src/test/resources/application.yml`.

6. **No API versioning strategy**: All endpoints are under `/api/v1/` but there is no versioning infrastructure in place
   for future breaking changes.

7. **No circuit breaker**: Services do not implement resilience patterns (e.g., Resilience4j) for fault tolerance.

8. **No distributed tracing**: No integration with tools like Jaeger or Zipkin for request tracing across services.

---

## Assumptions

1. **Customer types**: The task specifies customer types as retail, corporate, and investment. In this implementation,
   the `User` entity uses an `ADMIN`/`CUSTOMER` role enum. Customer type can be extended as a separate field.

2. **Address is optional at registration**: Address fields are stored in a separate `addresses` table linked to the
   user, but are not required during registration.

3. **Account balance starts at zero**: New accounts are initialized with a balance of 0.00.

4. **Single address per user**: While the data model supports multiple addresses via `@OneToMany`, only one address is
   typically managed per user.

5. **JWT expiration**: Tokens expire after 5 minutes (300,000 ms) for security. This can be adjusted via
   `spring.security.jwt.expiration` in `application.yml`.

6. **No transaction/transfer APIs**: This system handles account management only. Transaction and transfer features are
   out of scope.

7. **Event ordering**: The outbox publisher processes events in FIFO order (by `created_date`). The account-service
   processes events sequentially per partition.

---

## License

This project is for educational purposes.
