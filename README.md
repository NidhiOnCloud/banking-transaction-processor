# Banking Transaction Processor

A small REST service for account creation, deposits, withdrawals, transfers and per-account transaction history.

## Technology
- Java 21, Spring Boot 3.4, Maven
- Spring Web, Spring Data JPA, Bean Validation
- H2 in-memory database
- JUnit 5, Spring Boot Test and MockMvc

## Run
Requires JDK 21+ and Maven 3.8+.

```bash
mvn clean test
mvn spring-boot:run
```

The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:banking`, user `sa`, blank password).

## API

Create account:
```http
POST /api/accounts
Content-Type: application/json
{"accountId":"11111111-1111-1111-1111-111111111111","openingBalance":100.00}
```

Get balance: `GET /api/accounts/{id}`

Deposit:
```http
POST /api/accounts/{id}/deposits
{"amount":25.00}
```

Withdraw:
```http
POST /api/accounts/{id}/withdrawals
{"amount":10.00}
```

Transfer:
```http
POST /api/transfers
{"fromAccountId":"11111111-1111-1111-1111-111111111111","toAccountId":"22222222-2222-2222-2222-222222222222","amount":15.00}
```

History: `GET /api/accounts/{id}/transactions`

## Design decisions and trade-offs

1. **Money uses `BigDecimal`.** Binary floating-point types are unsuitable for currency. The API rejects values with more than two fractional digits rather than silently rounding.
2. **UUID account identifiers.** The caller supplies an ID so the service can explicitly demonstrate uniqueness validation. In a production API, the service would usually generate IDs.
3. **One ledger entry per account per movement.** A transfer writes a debit and credit entry with the same `referenceId`, allowing both account histories to correlate the transfer.
4. **Transfer atomicity.** The transfer method is transactional: both balance updates and both ledger entries commit together or roll back together.
5. **Concurrent updates.** Account rows are pessimistically locked for balance-changing operations. Transfers lock both accounts in deterministic UUID order to reduce deadlock risk. `@Version` is also retained as a defensive optimistic-locking mechanism.
6. **Simple persistence.** H2 and schema auto-update keep the kata runnable without external infrastructure. A production deployment should use PostgreSQL, schema migrations (Flyway/Liquibase), explicit database constraints, monitoring and backups.
7. **Error contract.** Missing accounts return 404, invalid operations return 400, and duplicate account IDs return 409.

## Known limitations / next improvements

- No authentication, authorization, idempotency keys, pagination or rate limiting.
- No explicit currency field; this kata assumes one currency per service instance.
- No external database or migration scripts.
- The test suite demonstrates key API behaviour but does not yet include a high-contention concurrency test or a full set of repository/service unit tests.
- A production-grade ledger would generally be append-only and derive or reconcile balances from immutable postings. This kata stores a current account balance alongside transaction entries for simplicity.
- Before production use, add database-level uniqueness/check constraints, idempotency, audit controls, structured error codes and operational observability.

## Refinement notes

The implementation deliberately keeps the public API small. During review, pay particular attention to whether validation belongs at the HTTP boundary, in the service, or both; whether locking strategy matches the database; and whether ledger entries are sufficient to explain every balance change. With more time, I would separate the domain model from JPA entities, introduce an immutable money/value object with currency, add idempotency and pagination, and test concurrent transfers against the production database engine.
