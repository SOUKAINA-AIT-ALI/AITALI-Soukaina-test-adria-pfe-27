package com.adria.bank.transaction.client;

import com.adria.bank.transaction.domain.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "account-service")
public interface AccountRestClient {

    @GetMapping("/api/accounts/{id}")
    AccountDTO getAccountById(@PathVariable("id") UUID id);

    @PostMapping("/api/accounts/{id}/debit")
    AccountDTO debit(@PathVariable("id") UUID id, @RequestParam("amount") double amount);

    @PostMapping("/api/accounts/{id}/credit")
    AccountDTO credit(@PathVariable("id") UUID id, @RequestParam("amount") double amount);
}

