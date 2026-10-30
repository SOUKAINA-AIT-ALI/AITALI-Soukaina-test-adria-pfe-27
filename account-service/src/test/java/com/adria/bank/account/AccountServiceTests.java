package com.adria.bank.account;

import com.adria.bank.account.domain.AccountResponseDTO;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.domain.CreateAccountDTO;
import com.adria.bank.account.service.AccountService;
import com.adria.bank.account.service.exception.AccountNotActiveException;
import com.adria.bank.account.service.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AccountServiceTests {

    @Autowired
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() {
        CreateAccountDTO request = new CreateAccountDTO("CUST-TEST", "MAD", 1000.0, AccountStatus.ACTIVE);
        AccountResponseDTO created = accountService.createAccount(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.customerReference()).isEqualTo("CUST-TEST");
        assertThat(created.balance()).isEqualTo(1000.0);
        assertThat(created.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(created.currency()).isEqualTo("MAD");
    }

    @Test
    void shouldDebitActiveAccountWithSufficientBalance() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AccountResponseDTO updated = accountService.debit(id, 500.0);

        assertThat(updated.balance()).isEqualTo(4500.0);
    }

    @Test
    void shouldThrowExceptionWhenDebitingMoreThanBalance() {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertThatThrownBy(() -> accountService.debit(id, 99999.0))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void shouldThrowExceptionWhenDebitingBlockedAccount() {
        UUID id = UUID.fromString("33333333-3333-3333-3333-333333333333");

        assertThatThrownBy(() -> accountService.debit(id, 10.0))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    void shouldCreditAccountSuccessfully() {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        AccountResponseDTO updated = accountService.credit(id, 500.0);

        assertThat(updated.balance()).isEqualTo(3000.0);
    }
}

