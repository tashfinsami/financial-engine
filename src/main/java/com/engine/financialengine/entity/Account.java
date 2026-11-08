package com.engine.financialengine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String nickname;

    private BigDecimal balance;

    @Version
    private Long version;

    protected Account() {
    }

    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}