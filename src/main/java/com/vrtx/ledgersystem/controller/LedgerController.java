package com.vrtx.ledgersystem.controller;

import com.vrtx.ledgersystem.dto.AccountLedgerResponse;
import com.vrtx.ledgersystem.dto.LedgerEntryResponse;
import com.vrtx.ledgersystem.entity.LedgerEntry;
import com.vrtx.ledgersystem.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/{id}/ledger")
    public ResponseEntity<AccountLedgerResponse> getAccountLedger(@PathVariable("id") UUID id) {
        LedgerService.AccountLedger ledger = ledgerService.getAccountLedger(id);

        List<LedgerEntryResponse> entries = ledger.entries().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(new AccountLedgerResponse(
                ledger.account().getId(),
                ledger.account().getAccountType().name(),
                ledger.account().getCurrency(),
                ledger.balance(),
                entries));
    }

    private LedgerEntryResponse toResponse(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getTransaction().getId(),
                entry.getEntryType().name(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getCreatedAt());
    }
}
