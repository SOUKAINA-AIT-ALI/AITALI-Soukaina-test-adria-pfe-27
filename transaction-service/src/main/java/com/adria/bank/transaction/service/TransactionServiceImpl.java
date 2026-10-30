package com.adria.bank.transaction.service;

import com.adria.bank.transaction.client.AccountRestClient;
import com.adria.bank.transaction.domain.*;
import com.adria.bank.transaction.repository.TransactionRepository;
import com.adria.bank.transaction.service.exception.AccountValidationException;
import com.adria.bank.transaction.service.exception.CurrencyMismatchException;
import com.adria.bank.transaction.service.exception.TransactionAmountExceededException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRestClient accountRestClient;

    @Value("${bank.transaction.max-amount:1000}")
    private double maxAmount;

    @Override
    public double getMaxAmountLimit() {
        return maxAmount;
    }

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        log.info("Traitement de la transaction pour le compte {}, montant {}", request.accountId(), request.amount());

        // 1. Règle métier : Montant strictement positif et inférieur ou égal au plafond configuré
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant de la transaction doit être strictement positif.");
        }
        if (request.amount().compareTo(BigDecimal.valueOf(maxAmount)) > 0) {
            throw new TransactionAmountExceededException(request.amount(), maxAmount);
        }

        // 2. Règle métier : Vérification du compte via OpenFeign avec disjoncteur Resilience4J
        AccountDTO account = getAccountWithCircuitBreaker(request.accountId());

        // Compte actif
        if (!"ACTIVE".equalsIgnoreCase(account.status())) {
            throw new AccountValidationException("Le compte " + request.accountId() + " n'est pas actif (statut: " + account.status() + ").");
        }

        // 3. Règle métier : Concordance des devises
        if (!request.currency().equalsIgnoreCase(account.currency())) {
            throw new CurrencyMismatchException(request.currency(), account.currency());
        }

        // 4. Règle métier : Consultation et mise à jour du solde du compte après débit
        accountRestClient.debit(request.accountId(), request.amount().doubleValue());

        // 5. Enregistrement de la transaction
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(request.accountId())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .merchantCategory(request.merchantCategory() != null ? request.merchantCategory() : "GENERAL")
                .country(request.country() != null ? request.country().toUpperCase() : "MA")
                .createdAt(Instant.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction enregistrée avec succès: id={}", saved.getId());
        return TransactionResponseDTO.fromEntity(saved);
    }

    @Override
    public TransactionResponseDTO getTransactionById(UUID id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable avec l'id: " + id));
        return TransactionResponseDTO.fromEntity(tx);
    }

    @Override
    public List<TransactionResponseDTO> getTransactionsByAccountId(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(TransactionResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void transfer(TransferRequestDTO request) {
        log.info("Traitement du virement: {} -> {}, montant: {}",
                request.sourceAccountId(), request.targetAccountId(), request.amount());

        // Plafond
        if (request.amount().compareTo(BigDecimal.valueOf(maxAmount)) > 0) {
            throw new TransactionAmountExceededException(request.amount(), maxAmount);
        }

        AccountDTO source = getAccountWithCircuitBreaker(request.sourceAccountId());
        AccountDTO target = getAccountWithCircuitBreaker(request.targetAccountId());

        if (!"ACTIVE".equalsIgnoreCase(source.status()) || !"ACTIVE".equalsIgnoreCase(target.status())) {
            throw new AccountValidationException("Les deux comptes doivent être actifs pour effectuer un virement.");
        }

        if (!request.currency().equalsIgnoreCase(source.currency()) || !request.currency().equalsIgnoreCase(target.currency())) {
            throw new CurrencyMismatchException(request.currency(), source.currency());
        }

        // Débit source et Crédit destination
        accountRestClient.debit(request.sourceAccountId(), request.amount().doubleValue());
        accountRestClient.credit(request.targetAccountId(), request.amount().doubleValue());

        // Enregistrement des 2 transactions
        Instant now = Instant.now();
        Transaction debitTx = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(request.sourceAccountId())
                .amount(request.amount().negate())
                .currency(request.currency().toUpperCase())
                .merchantCategory("TRANSFER_OUT")
                .country("MA")
                .createdAt(now)
                .build();

        Transaction creditTx = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(request.targetAccountId())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .merchantCategory("TRANSFER_IN")
                .country("MA")
                .createdAt(now)
                .build();

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);
        log.info("Virement de {} {} exécuté avec succès entre {} et {}",
                request.amount(), request.currency(), request.sourceAccountId(), request.targetAccountId());
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "accountFallback")
    public AccountDTO getAccountWithCircuitBreaker(UUID accountId) {
        return accountRestClient.getAccountById(accountId);
    }

    public AccountDTO accountFallback(UUID accountId, Throwable throwable) {
        log.error("Resilience4J Fallback déclenché pour le compte {}: {}", accountId, throwable.getMessage());
        throw new AccountValidationException("Le service compte (account-service) est actuellement indisponible ou a échoué. Cause: " + throwable.getMessage());
    }
}

