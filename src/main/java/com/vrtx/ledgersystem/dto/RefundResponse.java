package com.vrtx.ledgersystem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(
        UUID transactionId,
        UUID originalTransactionId,
        String status,
        String transactionType,
        BigDecimal amount,
        BigDecimal feeAmount,
        BigDecimal merchantAmount,
        Instant createdAt
) {
}
