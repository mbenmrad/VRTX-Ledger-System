package com.vrtx.ledgersystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementRequest(
        @NotBlank
        String idempotencyKey,

        @NotNull
        UUID merchantAccountId,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency
) {
}
