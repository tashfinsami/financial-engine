package com.engine.financialengine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.engine.financialengine.dto.TransactionRequest;
import com.engine.financialengine.service.AccountService;

@RestController
public class HomeController {

    public HomeController(AccountService accountService) {
    }

    @GetMapping("/")
    public String home() {
        return "Welcome to Financial Engine!";
    }

    @PostMapping("/api/transactions")
    public String createTransaction(@RequestBody TransactionRequest request) {

        return "Received amount: " + request.getAmount();
    }
}