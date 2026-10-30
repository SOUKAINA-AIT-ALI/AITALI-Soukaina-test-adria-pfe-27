package com.adria.bank.account.service;

import com.adria.bank.account.domain.AccountResponseDTO;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.domain.CreateAccountDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponseDTO createAccount(CreateAccountDTO request);

    AccountResponseDTO getAccountById(UUID id);

    List<AccountResponseDTO> getAllAccounts();

    AccountResponseDTO updateBalance(UUID id, double newBalance);

    AccountResponseDTO debit(UUID id, double amount);

    AccountResponseDTO credit(UUID id, double amount);

    AccountResponseDTO updateStatus(UUID id, AccountStatus status);
}

