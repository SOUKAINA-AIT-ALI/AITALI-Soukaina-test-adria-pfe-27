package com.adria.bank.transaction.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDTO(
        @NotNull(message = "sourceAccountId is required")
        UUID sourceAccountId,

        @NotNull(message = "targetAccountId is required")
        UUID targetAccountId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be strictly positive")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        String currency
) {
}

