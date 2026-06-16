# VRTX Ledger System

A double-entry accounting ledger built with Spring Boot and PostgreSQL. The system records payments, refunds, transfers and settlements as balanced ledger entries, and exposes a REST API to create transactions and inspect account balances.

## Key concepts

- **Double-entry accounting** — every transaction produces two or more balanced `ledger_entries` (debits and credits across accounts).
- **Immutable, append-only ledger** — entries are never updated or deleted. Reversals (e.g. refunds) are recorded as new entries that invert the original ones, not as edits.
- **Balance is never stored** — an account's balance is always derived on demand from its ledger entries (`SUM(CREDIT) - SUM(DEBIT)`), never persisted as a column.
- **Idempotency** — every transaction is created with a client-supplied `idempotencyKey`. Replaying the same key returns the original transaction instead of creating a duplicate.

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

The app starts on `http://localhost:8080`. Flyway runs `V1__init_schema.sql` and `V2__seed_data.sql` automatically on startup.

## Project structure

```
src/main/java/com/vrtx/ledgersystem/
├── entity/       # JPA entities (Account, Transaction, LedgerEntry)
├── repository/   # Spring Data repositories
├── service/      # Business logic (Payment, Refund, Transfer, Settlement, Ledger)
├── controller/   # REST controllers
├── dto/          # Request/response objects
└── exception/    # Custom exceptions and global error handling
```

## API

All transaction endpoints accept and return amounts as decimals and require an `idempotencyKey`. Calling the same endpoint twice with the same key returns the existing transaction instead of creating a new one.

### `POST /transactions/payment`

Charges a user account, credits the merchant net of a platform fee (`fees.payment.percentage` in `application.yml`, currently 3%), and credits the fee to the `FEES` account.

Request:
```json
{
  "idempotencyKey": "payment-001",
  "userAccountId": "11111111-1111-1111-1111-111111111111",
  "merchantAccountId": "22222222-2222-2222-2222-222222222222",
  "amount": 100.00,
  "currency": "EUR"
}
```

Response (`201 Created`):
```json
{
  "transactionId": "cbe23461-d72d-4aba-adf4-0621ebc4ce51",
  "status": "COMPLETED",
  "transactionType": "PAYMENT",
  "amount": 100.00,
  "feeAmount": 3.0000,
  "merchantAmount": 97.0000,
  "createdAt": "2026-06-16T20:22:18.059357Z"
}
```

### `POST /transactions/refund`

Reverses an existing transaction by inverting each of its ledger entries (`CREDIT` user, `DEBIT` merchant, `DEBIT` fees), and links the new transaction to the original via `related_transaction_id`.

Request:
```json
{
  "idempotencyKey": "refund-001",
  "originalTransactionId": "cbe23461-d72d-4aba-adf4-0621ebc4ce51"
}
```

Response (`201 Created`):
```json
{
  "transactionId": "8b33004f-c34b-4a93-beca-aaf275c2d594",
  "originalTransactionId": "cbe23461-d72d-4aba-adf4-0621ebc4ce51",
  "status": "COMPLETED",
  "transactionType": "REFUND",
  "amount": 100.0000,
  "feeAmount": 3.0000,
  "merchantAmount": 97.0000,
  "createdAt": "2026-06-16T20:23:35.558769Z"
}
```

### `POST /transactions/transfer`

Moves funds directly between two accounts. No fee is applied.

Request:
```json
{
  "idempotencyKey": "transfer-001",
  "sourceAccountId": "11111111-1111-1111-1111-111111111111",
  "destinationAccountId": "22222222-2222-2222-2222-222222222222",
  "amount": 50.00,
  "currency": "EUR"
}
```

Response (`201 Created`):
```json
{
  "transactionId": "feea1cdd-85ba-4c5d-9f6d-cd9fdbb25721",
  "status": "COMPLETED",
  "transactionType": "TRANSFER",
  "amount": 50.00,
  "createdAt": "2026-06-16T20:23:36.011292Z"
}
```

### `POST /transactions/settlement`

Pays out a merchant's balance to the platform's `SETTLEMENT` account (resolved automatically by `account_type`). No fee is applied.

Request:
```json
{
  "idempotencyKey": "settlement-001",
  "merchantAccountId": "22222222-2222-2222-2222-222222222222",
  "amount": 50.00,
  "currency": "EUR"
}
```

Response (`201 Created`):
```json
{
  "transactionId": "b2414d62-9cd9-4c24-b847-b5ba48985b44",
  "status": "COMPLETED",
  "transactionType": "SETTLEMENT",
  "amount": 50.00,
  "createdAt": "2026-06-16T20:23:36.300343Z"
}
```

### `GET /accounts/{id}/ledger`

Returns an account's balance — computed on the fly from its ledger entries — along with the full list of entries.

Response (`200 OK`):
```json
{
  "accountId": "11111111-1111-1111-1111-111111111111",
  "accountType": "USER",
  "currency": "EUR",
  "balance": 950.0000,
  "entries": [
    {
      "transactionId": "55555555-5555-5555-5555-555555555555",
      "entryType": "CREDIT",
      "amount": 1000.0000,
      "currency": "EUR",
      "createdAt": "2026-06-16T17:51:31.959260Z"
    },
    {
      "transactionId": "cbe23461-d72d-4aba-adf4-0621ebc4ce51",
      "entryType": "DEBIT",
      "amount": 100.0000,
      "currency": "EUR",
      "createdAt": "2026-06-16T20:22:18.486733Z"
    }
  ]
}
```

## Seed data

There is no `POST /accounts` endpoint yet, so `V2__seed_data.sql` preloads four test accounts plus an initial deposit, to make manual testing possible right after startup:

| Account | ID | Type | Currency |
|---|---|---|---|
| User | `11111111-1111-1111-1111-111111111111` | `USER` | EUR |
| Merchant | `22222222-2222-2222-2222-222222222222` | `MERCHANT` | EUR |
| Fees | `33333333-3333-3333-3333-333333333333` | `FEES` | EUR |
| Settlement | `44444444-4444-4444-4444-444444444444` | `SETTLEMENT` | EUR |

A seed `TRANSFER` transaction also credits the user account with an initial **1000.00 EUR** deposit (transferred from the `SETTLEMENT` account), so `payment`/`transfer` calls have funds to work with out of the box.

## Error handling

Errors are handled centrally by `GlobalExceptionHandler` (`@RestControllerAdvice`):

| Status | Cause | Example |
|---|---|---|
| `400 Bad Request` | DTO validation failure (`@Valid`) | missing/invalid `amount`, `currency`, or account ID |
| `404 Not Found` | Account or transaction not found | unknown `userAccountId`, `originalTransactionId`, etc. |
| `422 Unprocessable Entity` | Insufficient funds | source account balance lower than the requested amount |

## Design decisions

- **UUID primary keys** — accounts and transactions use `UUID` (not auto-increment integers) so IDs can be generated client- or server-side without coordination, and never leak sequential information about transaction volume.
- **BigDecimal for monetary amounts** — `amount` fields use `BigDecimal` (mapped to `NUMERIC(19,4)`) rather than `double`/`float`, since binary floating-point cannot represent decimal currency values exactly and would introduce rounding errors in financial calculations.
- **`@Transactional` service methods** — each transaction's ledger entries are written atomically: either all entries for a transaction are persisted, or none are, so the ledger can never end up in a partially-balanced state.
- **Unique `idempotency_key`** — enforced at the database level (`UNIQUE` constraint on `transactions.idempotency_key`), so retried requests (e.g. after a network timeout) are guaranteed to resolve to a single transaction even under concurrent calls, instead of relying solely on an application-level check.
