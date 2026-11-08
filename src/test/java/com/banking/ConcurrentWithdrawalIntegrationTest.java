package com.banking;

import com.banking.model.Account;
import com.banking.repository.AccountRepository;
import com.banking.exception.InsufficientBalanceException;

import com.banking.service.BankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrentWithdrawalIntegrationTest {

    @Autowired
    private BankingService bankingService;

    @Autowired
    private AccountRepository accountRepository;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        accountId = UUID.randomUUID();

        Account account = new Account(
                accountId,
                new BigDecimal("10000.00")
        );

        accountRepository.saveAndFlush(account);
    }

    @Test
    void shouldPreventOverdraftWhenTwoWithdrawalsOccurSimultaneously()
            throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> withdrawal1 = executor.submit(() -> {
                ready.countDown();
                start.await();

                try {
                    bankingService.withdraw(
                            accountId,
                            new BigDecimal("7000.00")
                    );
                    return true;
                } catch (InsufficientBalanceException exception) {
                    return false;
                }
            });

            Future<Boolean> withdrawal2 = executor.submit(() -> {
                ready.countDown();
                start.await();

                try {
                    bankingService.withdraw(
                            accountId,
                            new BigDecimal("5000.00")
                    );
                    return true;
                } catch (InsufficientBalanceException exception) {
                    return false;
                }
            });

            // Wait until both threads are ready.
            ready.await();

            // Release both withdrawal requests simultaneously.
            start.countDown();

            boolean result1 = withdrawal1.get();
            boolean result2 = withdrawal2.get();

            // Exactly one withdrawal must succeed.
            assertNotEquals(
                    result1,
                    result2,
                    "Exactly one withdrawal should succeed"
            );

            // Verify the final account balance.
            Account updatedAccount = accountRepository
                    .findById(accountId)
                    .orElseThrow();

            BigDecimal finalBalance = updatedAccount.getBalance();

            assertTrue(
                    finalBalance.compareTo(BigDecimal.ZERO) >= 0,
                    "Account balance must never be negative"
            );

            assertTrue(
                    finalBalance.compareTo(new BigDecimal("3000.00")) == 0
                    || finalBalance.compareTo(new BigDecimal("5000.00")) == 0,
                    "Final balance must reflect exactly one successful withdrawal"
            );

        } finally {
            executor.shutdownNow();
        }
    }
}