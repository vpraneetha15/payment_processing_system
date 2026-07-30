package com.example.model;

import java.math.BigDecimal;

public class Account {


    private String accountNumber;


    private String accountName;


    private BigDecimal balance;


    private String currency;


    private boolean active;


    public Account() {}


    public Account(String accountNumber, String accountName, BigDecimal balance,
                   String currency, boolean active) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
        this.currency = currency;
        this.active = active;
    }


    public String getAccountNumber() {
        return accountNumber;
    }


    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    public String getAccountName() {
        return accountName;
    }


    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }


    public BigDecimal getBalance() {
        return balance;
    }


    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }


    public String getCurrency() {
        return currency;
    }


    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public boolean isActive() {
        return active;
    }


    public void setActive(boolean active) {
        this.active = active;
    }

}