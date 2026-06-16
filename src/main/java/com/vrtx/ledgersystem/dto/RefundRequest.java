package com.vrtx.ledgersystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RefundRequest(
        @NotBlank
        String idempotencyKey,

        @NotNull
        UUID originalTransactionId
) {
}
