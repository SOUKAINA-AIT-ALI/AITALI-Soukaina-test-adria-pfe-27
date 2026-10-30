package com.adria.bank.transaction.mcp;

import com.adria.bank.transaction.domain.TransactionRequestDTO;
import com.adria.bank.transaction.domain.TransactionResponseDTO;
import com.adria.bank.transaction.domain.TransferRequestDTO;
import com.adria.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionMcpTools {

    private final TransactionService transactionService;

    public TransactionResponseDTO createTransaction(UUID accountId, BigDecimal amount, String currency, String merchantCategory, String country) {
        log.info("[MCP Tool] createTransaction: accountId={}, amount={}, currency={}", accountId, amount, currency);
        TransactionRequestDTO request = new TransactionRequestDTO(accountId, amount, currency, merchantCategory, country);
        return transactionService.createTransaction(request);
    }

    public String transferMoney(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, String currency) {
        log.info("[MCP Tool] transferMoney: from={}, to={}, amount={}, currency={}",
                sourceAccountId, targetAccountId, amount, currency);
        TransferRequestDTO request = new TransferRequestDTO(sourceAccountId, targetAccountId, amount, currency);
        transactionService.transfer(request);
        return String.format("Virement de %.2f %s du compte %s vers le compte %s exécuté avec succès.",
                amount, currency, sourceAccountId, targetAccountId);
    }

    public List<TransactionResponseDTO> getTransactionsByAccount(UUID accountId) {
        log.info("[MCP Tool] getTransactionsByAccount: accountId={}", accountId);
        return transactionService.getTransactionsByAccountId(accountId);
    }
}

