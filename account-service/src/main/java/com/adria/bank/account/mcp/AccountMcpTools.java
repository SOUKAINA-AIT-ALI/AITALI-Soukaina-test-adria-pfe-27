package com.adria.bank.account.mcp;

import com.adria.bank.account.domain.AccountResponseDTO;
import com.adria.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountMcpTools {

    private final AccountService accountService;

    public AccountResponseDTO getAccount(UUID accountId) {
        log.info("[MCP Tool] getAccount called with accountId={}", accountId);
        return accountService.getAccountById(accountId);
    }

    public String getAccountBalance(UUID accountId) {
        log.info("[MCP Tool] getAccountBalance called with accountId={}", accountId);
        AccountResponseDTO account = accountService.getAccountById(accountId);
        return String.format("Compte %s: Solde = %.2f %s (Statut: %s)",
                account.id(), account.balance(), account.currency(), account.status());
    }

    public String checkAccountStatus(UUID accountId) {
        log.info("[MCP Tool] checkAccountStatus called with accountId={}", accountId);
        AccountResponseDTO account = accountService.getAccountById(accountId);
        return String.format("Statut du compte %s: %s", account.id(), account.status());
    }

    public AccountResponseDTO debitAccount(UUID accountId, double amount) {
        log.info("[MCP Tool] debitAccount called with accountId={}, amount={}", accountId, amount);
        return accountService.debit(accountId, amount);
    }

    public AccountResponseDTO creditAccount(UUID accountId, double amount) {
        log.info("[MCP Tool] creditAccount called with accountId={}, amount={}", accountId, amount);
        return accountService.credit(accountId, amount);
    }
}

