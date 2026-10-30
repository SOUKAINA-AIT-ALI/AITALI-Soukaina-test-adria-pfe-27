package com.adria.bank.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String merchantCategory,
        String country,
        Instant createdAt
) {
    public static TransactionResponseDTO fromEntity(Transaction tx) {
        return new TransactionResponseDTO(
                tx.getId(),
                tx.getAccountId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getMerchantCategory(),
                tx.getCountry(),
                tx.getCreatedAt()
        );
    }
}

