package com.example.service;

import com.example.model.Payment;
import com.example.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PaymentNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationService.class);

    private final AccountRepository accountRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public PaymentNotificationService(AccountRepository accountRepository,
                                      ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.accountRepository = accountRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendPaymentCompletedNotifications(Payment payment) {
        if (payment == null || payment.getId() == null) {
            return;
        }

        String senderEmail = accountRepository.findEmailByAccountNumber(payment.getSourceAccount());
        String receiverEmail = accountRepository.findEmailByAccountNumber(payment.getDestinationAccount());

        sendIfAvailable(
                senderEmail,
                "Payment completed: " + payment.getId(),
                buildSenderBody(payment));

        sendIfAvailable(
                receiverEmail,
                "Incoming payment received: " + payment.getId(),
                buildReceiverBody(payment));
    }

    private void sendIfAvailable(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("Mail sender not configured; skipping notification to {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send notification email to {}: {}", to, ex.getMessage());
        }
    }

    private String buildSenderBody(Payment payment) {
        return "Your payment has been completed.\n"
                + "Payment ID: " + payment.getId() + "\n"
                + "From: " + payment.getSourceAccount() + "\n"
                + "To: " + payment.getDestinationAccount() + "\n"
                + "Amount: " + payment.getAmount() + " " + safe(payment.getCurrency()) + "\n"
                + "Status: COMPLETED\n";
    }

    private String buildReceiverBody(Payment payment) {
        return "You have received a payment.\n"
                + "Payment ID: " + payment.getId() + "\n"
                + "From: " + payment.getSourceAccount() + "\n"
                + "To: " + payment.getDestinationAccount() + "\n"
                + "Amount: " + payment.getAmount() + " " + safe(payment.getCurrency()) + "\n"
                + "Status: COMPLETED\n";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
