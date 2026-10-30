package com.adria.bank.account.service;

import com.adria.bank.account.domain.Account;
import com.adria.bank.account.domain.AccountResponseDTO;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.domain.CreateAccountDTO;
import com.adria.bank.account.repository.AccountRepository;
import com.adria.bank.account.service.exception.AccountNotActiveException;
import com.adria.bank.account.service.exception.AccountNotFoundException;
import com.adria.bank.account.service.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public AccountResponseDTO createAccount(CreateAccountDTO request) {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .customerReference(request.customerReference())
                .status(request.status() != null ? request.status() : AccountStatus.ACTIVE)
                .currency(request.currency().toUpperCase())
                .balance(request.initialBalance())
                .build();

        Account saved = accountRepository.save(account);
        log.info("Compte bancaire créé avec succès: id={}, client={}, solde={}",
                saved.getId(), saved.getCustomerReference(), saved.getBalance());
        return AccountResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return AccountResponseDTO.fromEntity(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public AccountResponseDTO updateBalance(UUID id, double newBalance) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        account.setBalance(newBalance);
        Account saved = accountRepository.save(account);
        log.info("Solde du compte {} mis à jour: nouveau solde={}", id, newBalance);
        return AccountResponseDTO.fromEntity(saved);
    }

    @Override
    public AccountResponseDTO debit(UUID id, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant du débit doit être strictement positif.");
        }
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(id);
        }

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException(id, account.getBalance(), amount);
        }

        account.setBalance(account.getBalance() - amount);
        Account saved = accountRepository.save(account);
        log.info("Débit de {} effectué sur le compte {}. Nouveau solde: {}", amount, id, saved.getBalance());
        return AccountResponseDTO.fromEntity(saved);
    }

    @Override
    public AccountResponseDTO credit(UUID id, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant du crédit doit être strictement positif.");
        }
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(id);
        }

        account.setBalance(account.getBalance() + amount);
        Account saved = accountRepository.save(account);
        log.info("Crédit de {} effectué sur le compte {}. Nouveau solde: {}", amount, id, saved.getBalance());
        return AccountResponseDTO.fromEntity(saved);
    }

    @Override
    public AccountResponseDTO updateStatus(UUID id, AccountStatus status) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        account.setStatus(status);
        Account saved = accountRepository.save(account);
        log.info("Statut du compte {} mis à jour: {}", id, status);
        return AccountResponseDTO.fromEntity(saved);
    }
}

