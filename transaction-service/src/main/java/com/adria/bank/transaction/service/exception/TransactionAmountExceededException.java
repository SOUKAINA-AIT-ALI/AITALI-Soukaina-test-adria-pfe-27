package com.adria.bank.transaction.service.exception;

import java.math.BigDecimal;

public class TransactionAmountExceededException extends RuntimeException {
    public TransactionAmountExceededException(BigDecimal amount, double maxAmount) {
        super(String.format("Le montant de la transaction (%.2f) dépasse le plafond autorisé (%.2f).", amount, maxAmount));
    }
}

