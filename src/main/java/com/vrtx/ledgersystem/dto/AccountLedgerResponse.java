package com.vrtx.ledgersystem.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AccountLedgerResponse(
        UUID accountId,
        String accountType,
        String currency,
        BigDecimal balance,
        List<LedgerEntryResponse> entries
) {
}
