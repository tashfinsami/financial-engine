package com.engine.financialengine.dto;

import jakarta.validation.constraints.NotBlank;

public class AccountRequest {

    @NotBlank
    private String accountNumber;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}