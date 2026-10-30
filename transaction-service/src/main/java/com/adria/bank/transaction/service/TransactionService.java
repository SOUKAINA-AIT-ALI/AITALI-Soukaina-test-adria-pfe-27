package com.adria.bank.transaction.service;

import com.adria.bank.transaction.domain.TransactionRequestDTO;
import com.adria.bank.transaction.domain.TransactionResponseDTO;
import com.adria.bank.transaction.domain.TransferRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponseDTO createTransaction(TransactionRequestDTO request);

    TransactionResponseDTO getTransactionById(UUID id);

    List<TransactionResponseDTO> getTransactionsByAccountId(UUID accountId);

    void transfer(TransferRequestDTO request);

    double getMaxAmountLimit();
}

