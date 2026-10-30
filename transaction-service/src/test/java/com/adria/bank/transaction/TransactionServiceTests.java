package com.adria.bank.transaction;

import com.adria.bank.transaction.client.AccountRestClient;
import com.adria.bank.transaction.domain.AccountDTO;
import com.adria.bank.transaction.domain.TransactionRequestDTO;
import com.adria.bank.transaction.domain.TransactionResponseDTO;
import com.adria.bank.transaction.domain.TransferRequestDTO;
import com.adria.bank.transaction.service.TransactionService;
import com.adria.bank.transaction.service.exception.AccountValidationException;
import com.adria.bank.transaction.service.exception.CurrencyMismatchException;
import com.adria.bank.transaction.service.exception.TransactionAmountExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class TransactionServiceTests {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private AccountRestClient accountRestClient;

    @Test
    void shouldCreateTransactionSuccessfully() {
        UUID accountId = UUID.randomUUID();
        AccountDTO activeAccount = new AccountDTO(accountId, "CUST-1", "ACTIVE", "MAD", 5000.0);

        when(accountRestClient.getAccountById(accountId)).thenReturn(activeAccount);
        when(accountRestClient.debit(eq(accountId), anyDouble())).thenReturn(activeAccount);

        TransactionRequestDTO request = new TransactionRequestDTO(
                accountId, new BigDecimal("250.00"), "MAD", "GROCERY", "MA"
        );

        TransactionResponseDTO response = transactionService.createTransaction(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.currency()).isEqualTo("MAD");
        verify(accountRestClient).debit(accountId, 250.00);
    }

    @Test
    void shouldRejectTransactionWhenAmountExceedsConfiguredLimit() {
        UUID accountId = UUID.randomUUID();
        // Plafond configuré à 1000
        TransactionRequestDTO request = new TransactionRequestDTO(
                accountId, new BigDecimal("1500.00"), "MAD", "ELECTRONICS", "MA"
        );

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(TransactionAmountExceededException.class);
    }

    @Test
    void shouldRejectTransactionWhenAccountIsNotActive() {
        UUID accountId = UUID.randomUUID();
        AccountDTO blockedAccount = new AccountDTO(accountId, "CUST-BLOCKED", "BLOCKED", "MAD", 5000.0);

        when(accountRestClient.getAccountById(accountId)).thenReturn(blockedAccount);

        TransactionRequestDTO request = new TransactionRequestDTO(
                accountId, new BigDecimal("100.00"), "MAD", "RETAIL", "MA"
        );

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(AccountValidationException.class)
                .hasMessageContaining("actif");
    }

    @Test
    void shouldRejectTransactionWhenCurrencyMismatch() {
        UUID accountId = UUID.randomUUID();
        AccountDTO accountInMad = new AccountDTO(accountId, "CUST-1", "ACTIVE", "MAD", 5000.0);

        when(accountRestClient.getAccountById(accountId)).thenReturn(accountInMad);

        // Transaction en EUR sur compte MAD
        TransactionRequestDTO request = new TransactionRequestDTO(
                accountId, new BigDecimal("50.00"), "EUR", "TRAVEL", "FR"
        );

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void shouldExecuteTransferSuccessfully() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        AccountDTO source = new AccountDTO(sourceId, "CUST-SRC", "ACTIVE", "MAD", 3000.0);
        AccountDTO target = new AccountDTO(targetId, "CUST-TGT", "ACTIVE", "MAD", 1000.0);

        when(accountRestClient.getAccountById(sourceId)).thenReturn(source);
        when(accountRestClient.getAccountById(targetId)).thenReturn(target);

        TransferRequestDTO transferRequest = new TransferRequestDTO(sourceId, targetId, new BigDecimal("500.00"), "MAD");
        transactionService.transfer(transferRequest);

        verify(accountRestClient).debit(sourceId, 500.00);
        verify(accountRestClient).credit(targetId, 500.00);

        assertThat(transactionService.getTransactionsByAccountId(sourceId)).hasSize(1);
        assertThat(transactionService.getTransactionsByAccountId(targetId)).hasSize(1);
    }
}

