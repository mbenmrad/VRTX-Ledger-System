package com.vrtx.ledgersystem.service;

import com.vrtx.ledgersystem.entity.Account;
import com.vrtx.ledgersystem.entity.LedgerEntry;
import com.vrtx.ledgersystem.entity.Transaction;
import com.vrtx.ledgersystem.repository.AccountRepository;
import com.vrtx.ledgersystem.repository.LedgerEntryRepository;
import com.vrtx.ledgersystem.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BigDecimal feePercentage;

    public PaymentService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           LedgerEntryRepository ledgerEntryRepository,
                           @Value("${fees.payment.percentage}") BigDecimal feePercentage) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.feePercentage = feePercentage;
    }

    @Transactional
    public PaymentResult processPayment(String idempotencyKey,
                                         UUID userAccountId,
                                         UUID merchantAccountId,
                                         BigDecimal amount,
                                         String currency) {
        BigDecimal fee = calculateFee(amount);
        BigDecimal merchantAmount = amount.subtract(fee);

        Transaction transaction = transactionRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> createPayment(idempotencyKey, userAccountId, merchantAccountId, amount, currency, fee, merchantAmount));

        return new PaymentResult(transaction, fee, merchantAmount);
    }

    private Transaction createPayment(String idempotencyKey,
                                       UUID userAccountId,
                                       UUID merchantAccountId,
                                       BigDecimal amount,
                                       String currency,
                                       BigDecimal fee,
                                       BigDecimal merchantAmount) {
        Account userAccount = getAccount(userAccountId);
        Account merchantAccount = getAccount(merchantAccountId);
        Account feesAccount = accountRepository.findByAccountType(Account.AccountType.FEES)
                .orElseThrow(() -> new EntityNotFoundException("FEES account not found"));

        // TODO: check balance

        Transaction transaction = transactionRepository.save(
                new Transaction(idempotencyKey, "Payment from user to merchant", null, Transaction.TransactionType.PAYMENT));

        ledgerEntryRepository.save(new LedgerEntry(transaction, userAccount, LedgerEntry.EntryType.DEBIT, amount, currency));
        ledgerEntryRepository.save(new LedgerEntry(transaction, merchantAccount, LedgerEntry.EntryType.CREDIT, merchantAmount, currency));
        ledgerEntryRepository.save(new LedgerEntry(transaction, feesAccount, LedgerEntry.EntryType.CREDIT, fee, currency));

        return transaction;
    }

    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(feePercentage).setScale(4, RoundingMode.HALF_UP);
    }

    private Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
    }

    public record PaymentResult(Transaction transaction, BigDecimal feeAmount, BigDecimal merchantAmount) {
    }
}
