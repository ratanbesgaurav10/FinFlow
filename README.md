# FinFlow — Financial Transaction Management System

A production-grade REST API backend built with **Spring Boot 3**, implementing secure financial transaction processing with JWT authentication, role-based access control, and full audit logging.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 17                             |
| Framework    | Spring Boot 3.2, Spring Security 6  |
| Persistence  | Spring Data JPA, Hibernate          |
| Database     | H2 (dev), PostgreSQL (prod)         |
| Auth         | JWT (JJWT 0.11.5)                   |
| Validation   | Jakarta Bean Validation             |
| API Docs     | SpringDoc OpenAPI / Swagger UI      |
| Testing      | JUnit 5, Mockito, MockMvc           |
| Build        | Maven 3.x                           |

---

## Architecture

```
src/main/java/com/finflow/
├── config/          # Security, JWT filter, OpenAPI, DataSeeder
├── controller/      # REST controllers (Auth, Account, Transaction, Admin)
├── dto/             # Request/Response DTOs with validation
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── model/           # JPA entities (User, Account, Transaction)
├── repository/      # Spring Data JPA repositories with custom queries
├── service/         # Service interfaces + implementations
└── util/            # JwtUtil, AccountNumberGenerator, ReferenceNumberGenerator
```

Key design decisions:
- **Layered architecture**: Controller → Service → Repository (separation of concerns)
- **SOLID principles**: Interface-based services, single-responsibility classes
- **Transactional safety**: `@Transactional` on all balance mutation operations
- **Custom DB indexes**: On `reference_number`, `status`, `created_at` for query performance
- **Unified API response**: `ApiResponse<T>` wrapper for consistent JSON structure

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run (H2 in-memory database — no setup required)

```bash
git clone https://github.com/YOUR_USERNAME/finflow.git
cd finflow
mvn spring-boot:run
```

The application starts at `http://localhost:8080`

### Demo credentials (auto-seeded on startup)

| Role  | Email               | Password    |
|-------|---------------------|-------------|
| Admin | admin@finflow.com   | Admin@1234  |
| User  | alice@finflow.com   | Alice@1234  |
| User  | bob@finflow.com     | Bob@12345   |

### Useful URLs

| URL                                      | Description         |
|------------------------------------------|---------------------|
| http://localhost:8080/swagger-ui.html    | Swagger UI (API docs + testing) |
| http://localhost:8080/h2-console         | H2 Database console |
| http://localhost:8080/actuator/health    | Health check        |

---

## API Reference

### Authentication
| Method | Endpoint               | Auth | Description           |
|--------|------------------------|------|-----------------------|
| POST   | /api/auth/register     | No   | Register new user     |
| POST   | /api/auth/login        | No   | Login, get JWT token  |

### Accounts
| Method | Endpoint                              | Auth | Description                  |
|--------|---------------------------------------|------|------------------------------|
| POST   | /api/accounts                         | Yes  | Create new account           |
| GET    | /api/accounts                         | Yes  | Get my accounts              |
| GET    | /api/accounts/{accountNumber}         | Yes  | Get account details          |
| GET    | /api/accounts/{accountNumber}/summary | Yes  | Get balance & transaction summary |
| PATCH  | /api/accounts/{accountNumber}/close   | Yes  | Close account                |

### Transactions
| Method | Endpoint                               | Auth | Description               |
|--------|----------------------------------------|------|---------------------------|
| POST   | /api/transactions/transfer             | Yes  | Fund transfer             |
| POST   | /api/transactions/deposit              | Yes  | Deposit funds             |
| POST   | /api/transactions/withdraw             | Yes  | Withdraw funds            |
| GET    | /api/transactions/my                   | Yes  | My transaction history    |
| GET    | /api/transactions/account/{number}     | Yes  | Account transactions      |
| GET    | /api/transactions/{referenceNumber}    | Yes  | Get by reference number   |

### Admin (ADMIN role only)
| Method | Endpoint                              | Auth  | Description           |
|--------|---------------------------------------|-------|-----------------------|
| GET    | /api/admin/dashboard                  | Admin | System statistics     |
| GET    | /api/admin/users                      | Admin | All users             |
| PATCH  | /api/admin/accounts/{number}/freeze   | Admin | Freeze account        |

---

## Sample API Flow

### 1. Register and get token
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"John Doe","email":"john@example.com","password":"Password@123"}'
```

### 2. Create an account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"accountType":"SAVINGS"}'
```

### 3. Deposit funds
```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"FF26031001","amount":5000,"description":"Initial deposit"}'
```

### 4. Transfer funds
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"sourceAccountNumber":"FF26031001","destinationAccountNumber":"FF26031002","amount":1000}'
```

---

## Running Tests

```bash
mvn test
```

Test coverage includes:
- Unit tests for `TransactionService` (transfer, deposit, withdrawal, insufficient funds)
- Integration tests for `AuthController` (register, login, validation errors)

---

## Production Setup (PostgreSQL)

```bash
mvn spring-boot:run -Dspring.profiles.active=postgres \
  -DDB_USERNAME=finflow \
  -DDB_PASSWORD=yourpassword
```

---

## Interview Notes

**Key technical decisions explained:**

1. **Why `@Transactional` on transfer?** — Both the debit and credit must succeed or both must roll back. Without it, a failure mid-transfer could deduct from source but not credit destination.

2. **Why interface-based services?** — Follows Dependency Inversion (SOLID). Makes unit testing with Mockito straightforward and allows swapping implementations.

3. **Why custom DB indexes?** — `reference_number` is queried on every transaction lookup. `status` and `created_at` support filtered reports. Reduces full-table scans significantly.

4. **Why H2 for dev?** — Zero-setup developer experience. The PostgreSQL profile (`application-postgres.properties`) is used in staging/production, keeping environment config separate.
