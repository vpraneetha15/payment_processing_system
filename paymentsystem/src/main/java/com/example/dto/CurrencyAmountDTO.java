package com.example.dto;

public class CurrencyAmountDTO {

    private String currency;
    private double amount;

    public CurrencyAmountDTO() {
    }

    public CurrencyAmountDTO(String currency, double amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}