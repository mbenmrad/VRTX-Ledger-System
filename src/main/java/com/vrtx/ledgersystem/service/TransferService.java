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
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransferService(AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public TransferResult processTransfer(String idempotencyKey,
                                           UUID sourceAccountId,
                                           UUID destinationAccountId,
                                           BigDecimal amount,
                                           String currency) {
        Transaction transaction = transactionRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> createTransfer(idempotencyKey, sourceAccountId, destinationAccountId, amount, currency));

        return new TransferResult(transaction, amount);
    }

    private Transaction createTransfer(String idempotencyKey,
                                        UUID sourceAccountId,
                                        UUID destinationAccountId,
                                        BigDecimal amount,
                                        String currency) {
        Account sourceAccount = getAccount(sourceAccountId);
        Account destinationAccount = getAccount(destinationAccountId);

        BigDecimal balance = ledgerEntryRepository.calculateBalance(sourceAccountId);
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account: " + sourceAccountId);
        }

        Transaction transaction = transactionRepository.save(
                new Transaction(idempotencyKey, "Transfer between accounts", null, Transaction.TransactionType.TRANSFER));

        ledgerEntryRepository.save(new LedgerEntry(transaction, sourceAccount, LedgerEntry.EntryType.DEBIT, amount, currency));
        ledgerEntryRepository.save(new LedgerEntry(transaction, destinationAccount, LedgerEntry.EntryType.CREDIT, amount, currency));

        return transaction;
    }

    private Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
    }

    public record TransferResult(Transaction transaction, BigDecimal amount) {
    }
}
