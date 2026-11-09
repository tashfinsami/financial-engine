package com.engine.financialengine.service;

import java.math.BigDecimal;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class RetryService {

    private final OptimisticLockingService optimisticLockingService;

    public RetryService(OptimisticLockingService optimisticLockingService) {
        this.optimisticLockingService = optimisticLockingService;
    }
    
    public void updateWithRetry(Long id, BigDecimal amount) {

        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {  // application-level retry mechanism
            try {
                optimisticLockingService.increaseBalance(id, amount);
                return;

            } catch (OptimisticLockingFailureException e) { // the exception is provided by Spring Data JPA
                if (attempt == maxAttempts) {
                    throw e;
                }
            }
        }
    }
}