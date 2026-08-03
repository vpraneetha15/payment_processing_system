package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.PaymentHistory;
import com.example.repository.PaymentHistoryRepository;

@Service
public class PaymentHistoryService {

    private PaymentHistoryRepository repository;

    public PaymentHistoryService(PaymentHistoryRepository repository) {
        this.repository = repository;
    }

    public int save(PaymentHistory paymentHistory) {
        return repository.save(paymentHistory);
    }

    public List<PaymentHistory> findAll() {
        return repository.findAll();
    }

    public List<PaymentHistory> findLatest(int limit) {
        return repository.findLatest(limit);
    }

    public PaymentHistory findById(String id) {
        return repository.findById(id);
    }

    public List<PaymentHistory> findByPaymentId(String paymentId) {
        return repository.findByPaymentId(paymentId);
    }

    public int update(PaymentHistory paymentHistory) {
        return repository.update(paymentHistory);
    }
}
