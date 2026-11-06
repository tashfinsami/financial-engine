package com.engine.financialengine.controller;

import com.engine.financialengine.dto.AccountRequest;
import com.engine.financialengine.dto.AccountUpdateRequest;
import com.engine.financialengine.entity.Account;
import com.engine.financialengine.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public Account createAccount(@Valid @RequestBody AccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @PatchMapping("/{id}")
    public Account updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {

        return accountService.updateAccount(id, request);
    }
}