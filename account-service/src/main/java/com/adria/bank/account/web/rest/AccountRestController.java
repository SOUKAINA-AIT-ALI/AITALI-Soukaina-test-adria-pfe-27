package com.adria.bank.account.web.rest;

import com.adria.bank.account.domain.AccountResponseDTO;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.domain.CreateAccountDTO;
import com.adria.bank.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountRestController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountDTO request) {
        AccountResponseDTO created = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountResponseDTO> updateBalance(@PathVariable UUID id, @RequestParam double balance) {
        return ResponseEntity.ok(accountService.updateBalance(id, balance));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<AccountResponseDTO> debit(@PathVariable UUID id, @RequestParam double amount) {
        return ResponseEntity.ok(accountService.debit(id, amount));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<AccountResponseDTO> credit(@PathVariable UUID id, @RequestParam double amount) {
        return ResponseEntity.ok(accountService.credit(id, amount));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AccountResponseDTO> updateStatus(@PathVariable UUID id, @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateStatus(id, status));
    }
}

