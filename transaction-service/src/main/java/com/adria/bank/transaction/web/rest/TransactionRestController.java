package com.adria.bank.transaction.web.rest;

import com.adria.bank.transaction.domain.TransactionRequestDTO;
import com.adria.bank.transaction.domain.TransactionResponseDTO;
import com.adria.bank.transaction.domain.TransferRequestDTO;
import com.adria.bank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionRestController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO created = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccountId(@PathVariable UUID accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequestDTO request) {
        transactionService.transfer(request);
        return ResponseEntity.ok(Map.of("message", "Virement effectué avec succès"));
    }

    @GetMapping("/limit")
    public ResponseEntity<Map<String, Object>> getMaxLimit() {
        return ResponseEntity.ok(Map.of("maxTransactionAmount", transactionService.getMaxAmountLimit()));
    }
}

