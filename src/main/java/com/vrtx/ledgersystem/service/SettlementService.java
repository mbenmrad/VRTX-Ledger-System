package com.vrtx.ledgersystem.service;

import com.vrtx.ledgersystem.entity.Account;
import com.vrtx.ledgersystem.entity.LedgerEntry;
import com.vrtx.ledgersystem.entity.Transaction;
import com.vrtx.ledgersystem.exception.InsufficientFundsException;
import com.vrtx.ledgersystem.repository.AccountRepository;
import com.vrtx.ledgersystem.repository.LedgerEntryRepository;
import com.vrtx.ledgersystem.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class SettlementService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public SettlementService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public SettlementResult processSettlement(String idempotencyKey,
                                               UUID merchantAccountId,
                                               BigDecimal amount,
                                               String currency) {
        Transaction transaction = transactionRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> createSettlement(idempotencyKey, merchantAccountId, amount, currency));

        return new SettlementResult(transaction, amount);
    }

    private Transaction createSettlement(String idempotencyKey,
                                          UUID merchantAccountId,
                                          BigDecimal amount,
                                          String currency) {
        Account merchantAccount = getAccount(merchantAccountId);
        Account settlementAccount = accountRepository.findByAccountType(Account.AccountType.SETTLEMENT)
                .orElseThrow(() -> new EntityNotFoundException("SETTLEMENT account not found"));

        BigDecimal balance = ledgerEntryRepository.calculateBalance(merchantAccountId);
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account: " + merchantAccountId);
        }

        Transaction transaction = transactionRepository.save(
                new Transaction(idempotencyKey, "Settlement payout to merchant", null, Transaction.TransactionType.SETTLEMENT));

        ledgerEntryRepository.save(new LedgerEntry(transaction, merchantAccount, LedgerEntry.EntryType.DEBIT, amount, currency));
        ledgerEntryRepository.save(new LedgerEntry(transaction, settlementAccount, LedgerEntry.EntryType.CREDIT, amount, currency));

        return transaction;
    }

    private Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
    }

    public record SettlementResult(Transaction transaction, BigDecimal amount) {
    }
}
