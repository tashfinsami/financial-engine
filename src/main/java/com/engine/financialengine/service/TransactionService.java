package com.engine.financialengine.service;

import org.springframework.stereotype.Service;

import com.engine.financialengine.dto.TransferRequest;
import com.engine.financialengine.entity.Account;
import com.engine.financialengine.repository.AccountRepository;

import com.engine.financialengine.exception.AccountNotFoundException;
import com.engine.financialengine.exception.InsufficientFundsException;

import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;

    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }    

    @Transactional
    public void transferMoney(TransferRequest request) {

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different"
            );
        }

        Long fromId = request.getFromAccountId();
        Long toId = request.getToAccountId();

        Account sender;
        Account receiver;

        if (fromId < toId) {    // lock accounts in ascending order of their IDs to prevent deadlocks for simultaneous transactions
            sender = accountRepository.findByIdForUpdate(fromId)
                    .orElseThrow(() -> new AccountNotFoundException(fromId));

            receiver = accountRepository.findByIdForUpdate(toId)
                    .orElseThrow(() -> new AccountNotFoundException(toId));

        } else {
            receiver = accountRepository.findByIdForUpdate(toId)
                    .orElseThrow(() -> new AccountNotFoundException(toId));

            sender = accountRepository.findByIdForUpdate(fromId)
                    .orElseThrow(() -> new AccountNotFoundException(fromId));
        }

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }

        sender.setBalance(
                sender.getBalance().subtract(request.getAmount())
        );

        receiver.setBalance(
                receiver.getBalance().add(request.getAmount())
        );
    }
}
