package com.adria.bank.account.service.exception;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(UUID id, double currentBalance, double requestedAmount) {
        super("Solde insuffisant sur le compte " + id + ". Solde actuel: " + currentBalance + ", montant requis: " + requestedAmount);
    }
}

