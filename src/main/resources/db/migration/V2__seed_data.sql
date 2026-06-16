INSERT INTO accounts (id, account_type, owner_reference, currency) VALUES
    ('11111111-1111-1111-1111-111111111111', 'USER', 'user-1', 'EUR'),
    ('22222222-2222-2222-2222-222222222222', 'MERCHANT', 'merchant-1', 'EUR'),
    ('33333333-3333-3333-3333-333333333333', 'FEES', NULL, 'EUR'),
    ('44444444-4444-4444-4444-444444444444', 'SETTLEMENT', NULL, 'EUR');

INSERT INTO transactions (id, idempotency_key, description, transaction_type) VALUES
    ('55555555-5555-5555-5555-555555555555', 'seed-deposit-user', 'Initial test deposit for user-1', 'TRANSFER');

INSERT INTO ledger_entries (transaction_id, account_id, entry_type, amount, currency) VALUES
    ('55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', 'DEBIT', 1000.0000, 'EUR'),
    ('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'CREDIT', 1000.0000, 'EUR');
