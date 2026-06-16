package com.vrtx.ledgersystem.service;

import com.vrtx.ledgersystem.entity.Account;
import com.vrtx.ledgersystem.entity.LedgerEntry;
import com.vrtx.ledgersystem.entity.Transaction;
import com.vrtx.ledgersystem.repository.LedgerEntryRepository;
import com.vrtx.ledgersystem.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public RefundService(TransactionRepository transactionRepository,
                          LedgerEntryRepository ledgerEntryRepository) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public RefundResult processRefund(String idempotencyKey, UUID originalTransactionId) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey)
                .map(refundTransaction -> {
                    EntryAmounts amounts = extractAmounts(ledgerEntryRepository.findByTransactionId(refundTransaction.getId()));
                    return new RefundResult(refundTransaction, amounts.userAmount(), amounts.feeAmount(), amounts.merchantAmount());
                })
                .orElseGet(() -> createRefund(idempotencyKey, originalTransactionId));
    }

    private RefundResult createRefund(String idempotencyKey, UUID originalTransactionId) {
        Transaction originalTransaction = transactionRepository.findById(originalTransactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + originalTransactionId));

        List<LedgerEntry> originalEntries = ledgerEntryRepository.findByTransactionId(originalTransactionId);

        // TODO: check original transaction hasn't already been refunded

        Transaction refundTransaction = transactionRepository.save(
                new Transaction(idempotencyKey, "Refund of transaction " + originalTransactionId, originalTransaction, Transaction.TransactionType.REFUND));

        for (LedgerEntry originalEntry : originalEntries) {
            LedgerEntry.EntryType reversedType = originalEntry.getEntryType() == LedgerEntry.EntryType.DEBIT
                    ? LedgerEntry.EntryType.CREDIT
                    : LedgerEntry.EntryType.DEBIT;

            ledgerEntryRepository.save(new LedgerEntry(
                    refundTransaction, originalEntry.getAccount(), reversedType, originalEntry.getAmount(), originalEntry.getCurrency()));
        }

        EntryAmounts amounts = extractAmounts(originalEntries);
        return new RefundResult(refundTransaction, amounts.userAmount(), amounts.feeAmount(), amounts.merchantAmount());
    }

    private EntryAmounts extractAmounts(List<LedgerEntry> entries) {
        BigDecimal userAmount = null;
        BigDecimal merchantAmount = null;
        BigDecimal feeAmount = null;

        for (LedgerEntry entry : entries) {
            switch (entry.getAccount().getAccountType()) {
                case USER -> userAmount = entry.getAmount();
                case MERCHANT -> merchantAmount = entry.getAmount();
                case FEES -> feeAmount = entry.getAmount();
                default -> { }
            }
        }

        return new EntryAmounts(userAmount, feeAmount, merchantAmount);
    }

    public record RefundResult(Transaction transaction, BigDecimal amount, BigDecimal feeAmount, BigDecimal merchantAmount) {
    }

    private record EntryAmounts(BigDecimal userAmount, BigDecimal feeAmount, BigDecimal merchantAmount) {
    }
}
