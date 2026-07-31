package com.example.dto;

public class TrendPointDTO {

    private String periodLabel;
    private long count;
    private double totalAmount;

    public TrendPointDTO() {
    }

    public TrendPointDTO(String periodLabel, long count, double totalAmount) {
        this.periodLabel = periodLabel;
        this.count = count;
        this.totalAmount = totalAmount;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
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
