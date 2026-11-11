package com.engine.financialengine;

import com.engine.financialengine.entity.Account;
import com.engine.financialengine.service.OptimisticLockingService;
import com.engine.financialengine.service.RetryService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import com.engine.financialengine.repository.AccountRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest 
@ActiveProfiles("test")
public class RetryServiceTest {

    @Autowired
    private RetryService retryService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoSpyBean
    private OptimisticLockingService optimisticLockingService;

    private TransactionTemplate createTransactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }
        
    @Test   // mock test to verify retry logic
    void shouldRetryAfterOptimisticLockingFailure() {

        OptimisticLockingService mockService =
                mock(OptimisticLockingService.class);

        RetryService retryService = new RetryService(mockService);

        BigDecimal amount = new BigDecimal("100");

        doThrow(new ObjectOptimisticLockingFailureException(Account.class, 2L)) // throw exception on first call
                .doNothing() // no exception on second call
                .when(mockService).increaseBalance(2L, amount);

        retryService.updateWithRetry(2L, amount);

        verify(mockService, times(2)).increaseBalance(2L, amount);
    }

    @Test   // integration test to verify retry logic with real services
    void shouldIncreaseBalanceUsingRealServices() {

        // create an account specifically for this test
        Account account = new Account(
                "TEST-RETRY-001",
                new BigDecimal("500")
        );

        account = accountRepository.save(account);

        Long accountId = account.getId();
        BigDecimal amount = new BigDecimal("100");

        // call the real RetryService
        retryService.updateWithRetry(accountId, amount);

        // check the updated balance
        BigDecimal balanceAfter = accountRepository.findById(accountId)
                .orElseThrow()
                .getBalance();

        BigDecimal expected = new BigDecimal("600");

        assertEquals(0, balanceAfter.compareTo(expected));
    }

    @Test   // test to simulate concurrent updates and verify retry logic works correctly
    void shouldHandleConcurrentUpdatesWithRetry() throws Exception {

        Account account = accountRepository.save(
                new Account("TEST-CONCURRENT-001", new BigDecimal("500"))
        );

        Long accountId = account.getId();

        clearInvocations(optimisticLockingService);

        CountDownLatch startSignal = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() -> {
                startSignal.await();
                retryService.updateWithRetry(accountId, new BigDecimal("100"));
                return null;
            });

            Future<?> second = executor.submit(() -> {
                startSignal.await();
                retryService.updateWithRetry(accountId, new BigDecimal("100"));
                return null;
            });

            startSignal.countDown();

            first.get();
            second.get();

            verify(optimisticLockingService, atLeast(3))    // check if the retry logic was triggered at least 3 times due to optimistic locking failures
                    .increaseBalance(accountId, new BigDecimal("100"));

            BigDecimal finalBalance = accountRepository.findById(accountId)
                    .orElseThrow()
                    .getBalance();

            assertEquals(0, finalBalance.compareTo(new BigDecimal("700")));

        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test   // test to reproduce optimistic locking conflict
    void shouldReproduceOptimisticLockingConflict() throws Exception {

        Account account = accountRepository.save(
                new Account("TEST-CONFLICT-001", new BigDecimal("500"))
        );

        Long accountId = account.getId();

        CountDownLatch bothLoaded = new CountDownLatch(2);
        CountDownLatch firstCommitted = new CountDownLatch(1);

        TransactionTemplate transactionTemplate = createTransactionTemplate();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> first = executor.submit(() -> {

            transactionTemplate.execute(status -> {

                Account firstAccount = accountRepository.findById(accountId)
                        .orElseThrow();

                bothLoaded.countDown();

                awaitLatch(bothLoaded);

                firstAccount.setBalance(
                        firstAccount.getBalance().add(new BigDecimal("100"))
                );

                return null;
            });

            // execute() returns only after the transaction commits.
            firstCommitted.countDown();

            return null;
        });

        Future<Boolean> second = executor.submit(() -> {

            try {
                transactionTemplate.execute(status -> {

                    Account secondAccount = accountRepository.findById(accountId)
                            .orElseThrow();

                    bothLoaded.countDown();

                    awaitLatch(bothLoaded);
                    awaitLatch(firstCommitted);

                    secondAccount.setBalance(
                            secondAccount.getBalance().add(new BigDecimal("200"))
                    );

                    return null;
                });

            } catch (OptimisticLockingFailureException e) {
                return true;
            }

            return false;
        });

        first.get();

        assertTrue(second.get());

        BigDecimal finalBalance = accountRepository.findById(accountId)
                .orElseThrow()
                .getBalance();

        assertEquals(0, finalBalance.compareTo(new BigDecimal("600")));

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
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
