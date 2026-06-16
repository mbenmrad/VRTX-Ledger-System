package com.vrtx.ledgersystem.controller;

import com.vrtx.ledgersystem.dto.PaymentRequest;
import com.vrtx.ledgersystem.dto.PaymentResponse;
import com.vrtx.ledgersystem.entity.Transaction;
import com.vrtx.ledgersystem.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentService.PaymentResult result = paymentService.processPayment(
                request.idempotencyKey(),
                request.userAccountId(),
                request.merchantAccountId(),
                request.amount(),
                request.currency());

        Transaction transaction = result.transaction();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new PaymentResponse(
                        transaction.getId(),
                        transaction.getStatus().name(),
                        transaction.getTransactionType().name(),
                        request.amount(),
                        result.feeAmount(),
                        result.merchantAmount(),
                        transaction.getCreatedAt()));
    }
}
