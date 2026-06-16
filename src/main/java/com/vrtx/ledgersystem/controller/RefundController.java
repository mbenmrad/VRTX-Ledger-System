package com.vrtx.ledgersystem.controller;

import com.vrtx.ledgersystem.dto.RefundRequest;
import com.vrtx.ledgersystem.dto.RefundResponse;
import com.vrtx.ledgersystem.entity.Transaction;
import com.vrtx.ledgersystem.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody RefundRequest request) {
        RefundService.RefundResult result = refundService.processRefund(
                request.idempotencyKey(),
                request.originalTransactionId());

        Transaction transaction = result.transaction();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new RefundResponse(
                        transaction.getId(),
                        request.originalTransactionId(),
                        transaction.getStatus().name(),
                        transaction.getTransactionType().name(),
                        result.amount(),
                        result.feeAmount(),
                        result.merchantAmount(),
                        transaction.getCreatedAt()));
    }
}
