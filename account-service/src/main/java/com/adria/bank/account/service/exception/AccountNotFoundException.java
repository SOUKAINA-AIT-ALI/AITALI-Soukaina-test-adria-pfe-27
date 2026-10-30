package com.adria.bank.account.service.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super("Compte introuvable avec l'identifiant: " + id);
    }
}

