package com.engine.financialengine.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.engine.financialengine.dto.AccountRequest;
import com.engine.financialengine.dto.AccountUpdateRequest;
import com.engine.financialengine.entity.Account;
import com.engine.financialengine.repository.AccountRepository;

import com.engine.financialengine.exception.AccountNotFoundException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(AccountRequest request) {

        Account account = new Account(
            request.getAccountNumber(),
            BigDecimal.ZERO
        );

        return accountRepository.save(account);
    }

    public Account getAccount(Long id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                    new AccountNotFoundException(id)
                );
    }

    public Account updateAccount(Long id, AccountUpdateRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        account.setNickname(request.getNickname());

        return accountRepository.save(account); //uses merge() internally to update existing entity
    }
}