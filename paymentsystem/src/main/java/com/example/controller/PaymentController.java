package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.dto.CurrencyAmountDTO;
import com.example.dto.PaymentSummaryDTO;
import com.example.model.Payment;
import com.example.service.PaymentService;

@RestController
@RequestMapping("/payments")
@Validated
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Payment payment) {
        try {
            service.save(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "INVALID_ACCOUNT",
                             "message", "Invalid account details. Ensure source and destination accounts exist."));
        }
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getPayments() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryDTO> getPaymentSummary() {
        return ResponseEntity.ok(service.getSummary());
    }

    @GetMapping("/amount-by-currency")
    public ResponseEntity<List<CurrencyAmountDTO>> getAmountByCurrency() {
        return ResponseEntity.ok(service.getAmountByCurrency());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable String id) {
        Payment payment = service.findById(id);
        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "PAYMENT_NOT_FOUND", "message", "Payment not found", "id", id));
        }
        return ResponseEntity.ok(payment);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody Payment payment) {
        int rows = service.update(payment);
        if (rows == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "PAYMENT_NOT_FOUND", "message", "Payment not found", "id", payment.getId()));
        }
        return ResponseEntity.ok(payment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        int rows = service.delete(id);
        if (rows == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "PAYMENT_NOT_FOUND", "message", "Payment not found", "id", id));
        }
        return ResponseEntity.ok(Map.of("message", "Payment deleted successfully", "id", id));
    }
}