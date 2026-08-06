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
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "VALIDATION_ERROR", "message", ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            Throwable rootCause = ex.getMostSpecificCause();
            String rootMsg = rootCause != null ? rootCause.getMessage() : ex.getMessage();
            boolean isAccountFkViolation = rootMsg != null
                    && (rootMsg.contains("payments_ibfk_1") || rootMsg.contains("payments_ibfk_2")
                        || rootMsg.contains("FOREIGN KEY"));
            if (isAccountFkViolation) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "INVALID_ACCOUNT",
                                 "message", "Invalid account details. Ensure source and destination accounts exist."));
            }
            return ResponseEntity.badRequest()
                .body(Map.of("error", "DATA_INTEGRITY_ERROR",
                             "message", rootMsg != null ? rootMsg : "Payment could not be saved due to a data integrity error."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "PAYMENT_ERROR",
                             "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error during payment processing"));
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

    /**
     * Returns a currency conversion preview.
     * Example: GET /payments/conversion-preview?from=USD&to=INR&amount=100
     */
    @GetMapping("/conversion-preview")
    public ResponseEntity<?> getConversionPreview(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "0") double amount) {

        List<String> supported = List.of("USD", "EUR", "GBP", "INR");
        if (!supported.contains(from.toUpperCase())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "UNSUPPORTED_CURRENCY", "message", "Unsupported source currency: " + from));
        }
        if (!supported.contains(to.toUpperCase())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "UNSUPPORTED_CURRENCY", "message", "Unsupported target currency: " + to));
        }
        if (amount <= 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "INVALID_AMOUNT", "message", "Amount must be greater than zero"));
        }

        double converted = service.convertAmount(amount, from, to);
        return ResponseEntity.ok(Map.of(
            "from", from.toUpperCase(),
            "to", to.toUpperCase(),
            "originalAmount", amount,
            "convertedAmount", converted,
            "sameCurrency", from.equalsIgnoreCase(to)
        ));
    }
}