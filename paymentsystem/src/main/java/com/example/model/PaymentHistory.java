package com.example.model;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

public class PaymentHistory {


    private String id;


    private String paymentId;


    private String status;


    private LocalDateTime timestamp;


    private String triggeredBy;


    private String note;


    public PaymentHistory() {}


    public PaymentHistory(String id, String paymentId, String status,
                          LocalDateTime timestamp, String triggeredBy, String note) {
        this.id = id;
        this.paymentId = paymentId;
        this.status = status;
        this.timestamp = timestamp;
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
        return timestamp;
    }


    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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


    public @Nullable Object getCreatedAt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCreatedAt'");
    }

}