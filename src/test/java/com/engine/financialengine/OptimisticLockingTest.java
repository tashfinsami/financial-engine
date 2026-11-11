package com.engine.financialengine;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.engine.financialengine.entity.Account;
import com.engine.financialengine.repository.AccountRepository;

import java.util.concurrent.ExecutionException;

@SpringBootTest
@ActiveProfiles("test")
class OptimisticLockingTest {

    private Long accountId;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void shouldDetectConcurrentUpdates() throws Exception {

        Account testAccount = accountRepository.save(
                new Account("TEST-OPTIMISTIC-001", new BigDecimal("1000"))
        );

        accountId = testAccount.getId();

        CountDownLatch bothLoaded = new CountDownLatch(2);
        CountDownLatch firstCommitted = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> first = executor.submit(() -> {
            transactionTemplate.execute(status -> {
                Account account = accountRepository.findById(accountId)
                        .orElseThrow();

                bothLoaded.countDown();
                awaitLatch(bothLoaded);

                account.setBalance(
                    account.getBalance().add(new BigDecimal("100"))
                );

                return null;
            });

            firstCommitted.countDown();
        });

        Future<?> second = executor.submit(() -> {
            transactionTemplate.execute(status -> {
                Account account = accountRepository.findById(accountId)
                        .orElseThrow();

                bothLoaded.countDown();

                awaitLatch(firstCommitted);

                account.setBalance(
                    account.getBalance().add(new BigDecimal("200"))
                );

                return null;
            });
        });

        first.get();

        ExecutionException exception =
                assertThrows(ExecutionException.class, second::get);

        System.out.println(exception.getCause());

        executor.shutdown();
    }

    @AfterEach
    void cleanUp() {
        if (accountId != null) {
            accountRepository.deleteById(accountId);
        }
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}