package com.adria.bank.account.web.graphql;

import com.adria.bank.account.domain.AccountResponseDTO;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.domain.CreateAccountDTO;
import com.adria.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountGraphQLController {

    private final AccountService accountService;

    @QueryMapping
    public List<AccountResponseDTO> accounts() {
        return accountService.getAllAccounts();
    }

    @QueryMapping
    public AccountResponseDTO accountById(@Argument("id") UUID id) {
        return accountService.getAccountById(id);
    }

    @MutationMapping
    public AccountResponseDTO createAccount(@Argument("input") CreateAccountDTO input) {
        return accountService.createAccount(input);
    }

    @MutationMapping
    public AccountResponseDTO debitAccount(@Argument("id") UUID id, @Argument("amount") double amount) {
        return accountService.debit(id, amount);
    }

    @MutationMapping
    public AccountResponseDTO creditAccount(@Argument("id") UUID id, @Argument("amount") double amount) {
        return accountService.credit(id, amount);
    }

    @MutationMapping
    public AccountResponseDTO updateStatus(@Argument("id") UUID id, @Argument("status") AccountStatus status) {
        return accountService.updateStatus(id, status);
    }
}
