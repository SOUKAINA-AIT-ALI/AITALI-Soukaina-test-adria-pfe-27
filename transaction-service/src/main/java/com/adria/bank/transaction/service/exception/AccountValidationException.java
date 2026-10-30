package com.adria.bank.transaction.service.exception;

public class AccountValidationException extends RuntimeException {
    public AccountValidationException(String message) {
        super(message);
    }
}

