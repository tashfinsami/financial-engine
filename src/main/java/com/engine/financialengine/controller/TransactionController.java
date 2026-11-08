package com.engine.financialengine.controller;

import com.engine.financialengine.dto.TransferRequest;
import com.engine.financialengine.service.TransactionService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public String transferMoney(@Valid @RequestBody TransferRequest request) {

        transactionService.transferMoney(request);

        return "Transfer completed successfully";
    }
}