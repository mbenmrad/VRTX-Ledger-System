package com.vrtx.ledgersystem.service;

import com.vrtx.ledgersystem.entity.Account;
import com.vrtx.ledgersystem.entity.LedgerEntry;
import com.vrtx.ledgersystem.repository.AccountRepository;
import com.vrtx.ledgersystem.repository.LedgerEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(AccountRepository accountRepository,
                          LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public AccountLedger getAccountLedger(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));

        BigDecimal balance = ledgerEntryRepository.calculateBalance(accountId);
        List<LedgerEntry> entries = ledgerEntryRepository.findByAccountId(accountId);

        return new AccountLedger(account, balance, entries);
    }

    public record AccountLedger(Account account, BigDecimal balance, List<LedgerEntry> entries) {
    }
}
