package com.adria.bank.account.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountDTO(
        @NotBlank(message = "customerReference cannot be blank")
        String customerReference,

        @NotBlank(message = "currency is required")
        String currency,

        @PositiveOrZero(message = "initial balance must be positive or zero")
        double initialBalance,

        AccountStatus status
) {
}

