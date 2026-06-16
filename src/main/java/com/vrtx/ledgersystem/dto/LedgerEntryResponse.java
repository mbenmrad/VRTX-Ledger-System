package com.vrtx.ledgersystem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID transactionId,
        String entryType,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
