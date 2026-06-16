CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('USER', 'MERCHANT', 'FEES', 'SETTLEMENT')),
    owner_reference VARCHAR(100),
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    related_transaction_id UUID REFERENCES transactions(id),
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('PAYMENT', 'REFUND', 'TRANSFER', 'SETTLEMENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    entry_type VARCHAR(6) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_accounts_type ON accounts(account_type);
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);
