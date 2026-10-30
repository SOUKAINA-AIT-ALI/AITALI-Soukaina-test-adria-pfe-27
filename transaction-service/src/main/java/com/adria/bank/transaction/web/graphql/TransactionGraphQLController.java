package com.adria.bank.transaction.web.graphql;

import com.adria.bank.transaction.domain.TransactionRequestDTO;
import com.adria.bank.transaction.domain.TransactionResponseDTO;
import com.adria.bank.transaction.domain.TransferRequestDTO;
import com.adria.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {

    private final TransactionService transactionService;

    @QueryMapping
    public List<TransactionResponseDTO> transactionsByAccount(@Argument("accountId") UUID accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }

    @QueryMapping
    public TransactionResponseDTO transactionById(@Argument("id") UUID id) {
        return transactionService.getTransactionById(id);
    }

    @QueryMapping
    public double maxTransactionLimit() {
        return transactionService.getMaxAmountLimit();
    }

    @MutationMapping
    public TransactionResponseDTO createTransaction(@Argument("input") TransactionRequestDTO input) {
        return transactionService.createTransaction(input);
    }

    @MutationMapping
    public String executeTransfer(@Argument("input") TransferRequestDTO input) {
        transactionService.transfer(input);
        return "Virement effectué avec succès";
    }
}

