package com.example.dto;

public class CurrencyVolumeDTO {

    private String currency;
    private long count;
    private double totalAmount;

    public CurrencyVolumeDTO() {
    }

    public CurrencyVolumeDTO(String currency, long count, double totalAmount) {
        this.currency = currency;
        this.count = count;
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
