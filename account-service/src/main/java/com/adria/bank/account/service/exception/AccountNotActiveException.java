package com.adria.bank.account.service.exception;

import java.util.UUID;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(UUID id) {
        super("Le compte " + id + " n'est pas actif (statut BLOCKED). Opération refusée.");
    }
}

