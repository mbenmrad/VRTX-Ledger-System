package com.vrtx.ledgersystem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transactionId,
        String status,
        String transactionType,
        BigDecimal amount,
        Instant createdAt
) {
}
