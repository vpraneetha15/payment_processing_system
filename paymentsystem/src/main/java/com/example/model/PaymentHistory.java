package com.example.model;

import java.time.LocalDateTime;

public class PaymentHistory {


    private String id;


    private String paymentId;


    private String status;


    private LocalDateTime createdAt;


    private String triggeredBy;


    private String note;


    public PaymentHistory() {}


    public PaymentHistory(String id, String paymentId, String status,
                          LocalDateTime timestamp, String triggeredBy, String note) {
        this.id = id;
        this.paymentId = paymentId;
        this.status = status;
        this.createdAt = timestamp;
        this.triggeredBy = triggeredBy;
        this.note = note;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getPaymentId() {
        return paymentId;
    }


    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getTimestamp() {
        return createdAt;
    }


    public void setTimestamp(LocalDateTime timestamp) {
        this.createdAt = timestamp;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public String getTriggeredBy() {
        return triggeredBy;
    }


    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }


    public String getNote() {
        return note;
    }


    public void setNote(String note) {
        this.note = note;
    }


}