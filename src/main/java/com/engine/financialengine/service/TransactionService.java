package com.engine.financialengine.service;

import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    public String processTransaction() {
        return "Transaction processing started!";
    }
}