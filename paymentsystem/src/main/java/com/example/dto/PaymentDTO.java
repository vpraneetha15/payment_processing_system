package com.example.dto;

public class PaymentDTO {

    private double amount;
    private String currency;
    private String sourceAccount;
    private String destinationAccount;

    public PaymentDTO() {
    }

    public PaymentDTO(double amount,
            String currency,
            String sourceAccount,
            String destinationAccount) {

        this.amount = amount;
        this.currency = currency;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }
}
