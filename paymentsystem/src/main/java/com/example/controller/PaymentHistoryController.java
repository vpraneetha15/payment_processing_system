package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.PaymentHistory;
import com.example.service.PaymentHistoryService;

@RestController
@RequestMapping("/payment-history")
@CrossOrigin(origins = "*")
public class PaymentHistoryController {

    private final PaymentHistoryService service;

    public PaymentHistoryController(PaymentHistoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentHistory> save(@RequestBody PaymentHistory paymentHistory) {
        service.save(paymentHistory);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentHistory);
    }

    @GetMapping
    public ResponseEntity<List<PaymentHistory>> getPaymentHistory() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<PaymentHistory>> getLatestHistory(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.findLatest(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHistory(@PathVariable String id) {
        PaymentHistory history = service.findById(id);
        if (history == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "HISTORY_NOT_FOUND", "message", "History record not found", "id", id));
        }
        return ResponseEntity.ok(history);
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<PaymentHistory>> getHistoryByPaymentId(@PathVariable String paymentId) {
        return ResponseEntity.ok(service.findByPaymentId(paymentId));
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody PaymentHistory paymentHistory) {
        int rows = service.update(paymentHistory);
        if (rows == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "HISTORY_NOT_FOUND", "message", "History record not found", "id", paymentHistory.getId()));
        }
        return ResponseEntity.ok(paymentHistory);
    }
}
