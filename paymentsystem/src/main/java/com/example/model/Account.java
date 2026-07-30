package com.example.model;


import jakarta.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name="accounts")
public class Account {


    @Id
    @Column(name = "account_number", length = 30)
    private String accountNumber;


    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;


    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;


    @Column(nullable = false, length = 3)
    private String currency;


    @Column(nullable = false)
    private boolean active = true;


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