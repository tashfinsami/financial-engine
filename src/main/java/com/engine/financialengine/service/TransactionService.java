package com.engine.financialengine.service;

import org.springframework.stereotype.Service;

import com.engine.financialengine.repository.AccountRepository;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;

    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String processTransaction() {
        return "Transaction processing started!";
    }

    public long getAccountCount() {
        return accountRepository.count();
    }
}