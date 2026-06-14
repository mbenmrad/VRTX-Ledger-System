# VRTX Ledger System

A double-entry accounting ledger built with Spring Boot and PostgreSQL.

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Database | PostgreSQL 16 |
| Migrations | Flyway 9 |
| Build | Maven |

## Prerequisites

- Java 21
- Docker (for PostgreSQL)
- IntelliJ IDEA (recommended)

## Getting started

**1. Start the database**

```bash
docker-compose up -d
```

**2. Run the application**

From IntelliJ: run `LedgerSystemApplication.java` directly.

Or from the terminal:

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

## Project structure

```
src/main/java/com/vrtx/ledgersystem/
├── entity/       # JPA entities
├── repository/   # Spring Data repositories
├── service/      # Business logic
├── controller/   # REST controllers
├── dto/          # Request/response objects
└── exception/    # Custom exceptions and error handling
```

## Status

> Work in progress — database schema and business logic are not yet implemented.
