package com.example;

import com.example.model.Payment;
import com.example.model.PaymentHistory;
import com.example.repository.PaymentHistoryRepository;
import com.example.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class PaymentStatusScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    public PaymentStatusScheduler(PaymentRepository paymentRepository,
                                   PaymentHistoryRepository paymentHistoryRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
    }

    @Scheduled(fixedDelay = 5000)
    public void processPaymentStatuses() {
        List<Payment> payments = paymentRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Payment payment : payments) {
            if (payment.getCreatedAt() == null) continue;

            String status = payment.getStatus();
            long secondsElapsed = Duration.between(payment.getCreatedAt(), now).getSeconds();

            if ("CREATED".equals(status) && secondsElapsed >= 10) {
                advanceStatus(payment, "VALIDATED", "Payment validated successfully");
            } else if ("VALIDATED".equals(status) && secondsElapsed >= 20) {
                String finalStatus = Math.random() < 0.9 ? "SUCCESS" : "FAILED";
                advanceStatus(payment, finalStatus,
                        "SUCCESS".equals(finalStatus)
                                ? "Payment processed successfully"
                                : "Payment processing failed");
            }
        }
    }

    private void advanceStatus(Payment payment, String newStatus, String note) {
        payment.setStatus(newStatus);
        paymentRepository.update(payment);

        PaymentHistory history = new PaymentHistory();
        history.setId(UUID.randomUUID().toString());
        history.setPaymentId(payment.getId());
        history.setStatus(newStatus);
        history.setCreatedAt(LocalDateTime.now());
        history.setTriggeredBy("SCHEDULER");
        history.setNote(note);
        paymentHistoryRepository.save(history);
    }
}
