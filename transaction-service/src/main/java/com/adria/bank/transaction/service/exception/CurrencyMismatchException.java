package com.adria.bank.transaction.service.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String txCurrency, String accountCurrency) {
        super(String.format("La devise de la transaction (%s) ne correspond pas à celle du compte (%s).", txCurrency, accountCurrency));
    }
}

