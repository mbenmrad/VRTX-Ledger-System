package com.vrtx.ledgersystem.controller;

import com.vrtx.ledgersystem.dto.TransferRequest;
import com.vrtx.ledgersystem.dto.TransferResponse;
import com.vrtx.ledgersystem.entity.Transaction;
import com.vrtx.ledgersystem.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        TransferService.TransferResult result = transferService.processTransfer(
                request.idempotencyKey(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.currency());

        Transaction transaction = result.transaction();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new TransferResponse(
                        transaction.getId(),
                        transaction.getStatus().name(),
                        transaction.getTransactionType().name(),
                        result.amount(),
                        transaction.getCreatedAt()));
    }
}
