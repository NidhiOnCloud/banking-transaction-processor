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