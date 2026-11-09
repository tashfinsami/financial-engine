package com.engine.financialengine.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.engine.financialengine.repository.AccountRepository;
import org.springframework.transaction.annotation.Transactional;
import com.engine.financialengine.entity.Account;
import com.engine.financialengine.exception.AccountNotFoundException;

@Service
public class OptimisticLockingService {

    private final AccountRepository accountRepository;

    public OptimisticLockingService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void increaseBalance(Long id, BigDecimal amount) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        account.setBalance(
                account.getBalance().add(amount)
        );
    }
}