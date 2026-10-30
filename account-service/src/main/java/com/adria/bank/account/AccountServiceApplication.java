package com.adria.bank.account;

import com.adria.bank.account.domain.Account;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(AccountRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // Compte 1 - Actif en MAD
                repository.save(Account.builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .customerReference("CUST-1001")
                        .status(AccountStatus.ACTIVE)
                        .currency("MAD")
                        .balance(5000.0)
                        .build());

                // Compte 2 - Actif en MAD
                repository.save(Account.builder()
                        .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .customerReference("CUST-1002")
                        .status(AccountStatus.ACTIVE)
                        .currency("MAD")
                        .balance(2500.0)
                        .build());

                // Compte 3 - Bloqué
                repository.save(Account.builder()
                        .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                        .customerReference("CUST-1003")
                        .status(AccountStatus.BLOCKED)
                        .currency("MAD")
                        .balance(100.0)
                        .build());
            }
        };
    }
}

