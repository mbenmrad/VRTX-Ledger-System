package com.vrtx.ledgersystem.controller;

import com.vrtx.ledgersystem.dto.SettlementRequest;
import com.vrtx.ledgersystem.dto.SettlementResponse;
import com.vrtx.ledgersystem.entity.Transaction;
import com.vrtx.ledgersystem.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/settlement")
    public ResponseEntity<SettlementResponse> createSettlement(@Valid @RequestBody SettlementRequest request) {
        SettlementService.SettlementResult result = settlementService.processSettlement(
                request.idempotencyKey(),
                request.merchantAccountId(),
                request.amount(),
                request.currency());

        Transaction transaction = result.transaction();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SettlementResponse(
                        transaction.getId(),
                        transaction.getStatus().name(),
                        transaction.getTransactionType().name(),
                        result.amount(),
                        transaction.getCreatedAt()));
    }
}
