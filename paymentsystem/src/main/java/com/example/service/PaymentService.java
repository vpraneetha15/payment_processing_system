package com.example.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;


import org.springframework.stereotype.Service;

import com.example.dto.CurrencyAmountDTO;
import com.example.dto.PaymentDTO;
import com.example.dto.PaymentSummaryDTO;
import com.example.exception.PaymentNotFoundException;
import com.example.model.Account;
import com.example.model.Payment;
import com.example.model.PaymentHistory;
import com.example.repository.AccountRepository;
import com.example.repository.PaymentHistoryRepository;
import com.example.repository.PaymentRepository;



@Service
public class PaymentService {


private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "EUR", "GBP", "INR");
private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");
private static final BigDecimal SEED_BALANCE = new BigDecimal("500000");

private static final int VALIDATION_FAILURE_PCT = 4;
private static final int NETWORK_FAILURE_PCT = 5;
private static final int PROCESSING_FAILURE_PCT = 3;

private final Random random = new Random();

private PaymentRepository repository;
private AccountRepository accountRepository;
private PaymentHistoryRepository paymentHistoryRepository;
private PaymentNotificationService paymentNotificationService;


public PaymentService(PaymentRepository repository,
	AccountRepository accountRepository,
	PaymentHistoryRepository paymentHistoryRepository,
	PaymentNotificationService paymentNotificationService){

this.repository=repository;
this.accountRepository=accountRepository;
this.paymentHistoryRepository=paymentHistoryRepository;
this.paymentNotificationService=paymentNotificationService;

}



public int save(PaymentDTO dto) {

Payment payment = new Payment();

payment.setAmount(dto.getAmount());
payment.setCurrency(dto.getCurrency());
payment.setSourceAccount(dto.getSourceAccount());
payment.setDestinationAccount(dto.getDestinationAccount());

ensureAccountExists(payment.getSourceAccount(), payment.getCurrency());
ensureAccountExists(payment.getDestinationAccount(), payment.getCurrency());

payment.setStatus("CREATED");
payment.setCreatedAt(LocalDateTime.now());

return save(payment);

}


public int save(Payment payment) {

    if (payment.getId() == null || payment.getId().isBlank()) {
        payment.setId(UUID.randomUUID().toString());
    }

	if (payment.getStatus() == null || payment.getStatus().isBlank()) {
		payment.setStatus("CREATED");
	}
	if (payment.getCreatedAt() == null) {
		payment.setCreatedAt(LocalDateTime.now());
	}

	ensureAccountExists(payment.getSourceAccount(), payment.getCurrency());
	ensureAccountExists(payment.getDestinationAccount(), payment.getCurrency());

	int rowsAffected = repository.save(payment);
	if (rowsAffected > 0) {
		saveHistoryEntry(payment);
		simulateProcessing(payment);
	}

	return rowsAffected;

}


private void saveHistoryEntry(Payment payment) {

	PaymentHistory history = new PaymentHistory();
	history.setId(UUID.randomUUID().toString());
	history.setPaymentId(payment.getId());
	history.setStatus(payment.getStatus());
	history.setCreatedAt(payment.getCreatedAt());
	history.setTriggeredBy("API");
	history.setNote("Payment created");

	paymentHistoryRepository.save(history);

}


/**
 * Simulates internal payment processing (per training brief: no real payment
 * network integration is required). Progresses a freshly CREATED payment
 * through VALIDATED -> SENT -> COMPLETED, or fails it at any stage with an
 * appropriate error code, writing a full audit trail to payment_history.
 */
private void simulateProcessing(Payment payment) {

	String validationError = detectValidationFailure(payment);
	if (validationError == null && random.nextInt(100) < VALIDATION_FAILURE_PCT) {
		validationError = "VALIDATION_FAILED";
	}
	if (validationError != null) {
		markFailed(payment, validationError, "Payment failed validation checks");
		return;
	}
	recordTransition(payment, "VALIDATED", "SYSTEM", "Payment passed validation checks");

	if (random.nextInt(100) < NETWORK_FAILURE_PCT) {
		markFailed(payment, "NETWORK_ERROR", "Communication failure with payment network");
		return;
	}
	recordTransition(payment, "SENT", "SYSTEM", "Payment transmitted to destination network");

	Account source = accountRepository.findById(payment.getSourceAccount());

	if (hasInsufficientFunds(payment, source)) {
		markFailed(payment, "INSUFFICIENT_FUNDS", "Source account has insufficient funds");
		return;
	}

	if (random.nextInt(100) < PROCESSING_FAILURE_PCT) {
		markFailed(payment, "PROCESSING_ERROR", "Internal error during payment processing");
		return;
	}

	settleFunds(payment, source);

	payment.setStatus("COMPLETED");
	payment.setErrorCode(null);
	repository.update(payment);
	recordTransition(payment, "COMPLETED", "SYSTEM", "Payment completed successfully");
	paymentNotificationService.sendPaymentCompletedNotifications(payment);

}


private String detectValidationFailure(Payment payment) {

	if (payment.getAmount() <= 0 || payment.getAmount() > MAX_AMOUNT.doubleValue()) {
		return "INVALID_AMOUNT";
	}

	if (payment.getCurrency() == null
			|| !SUPPORTED_CURRENCIES.contains(payment.getCurrency().toUpperCase())) {
		return "INVALID_CURRENCY";
	}

	if (payment.getSourceAccount() == null
			|| payment.getSourceAccount().isBlank()
			|| payment.getDestinationAccount() == null
			|| payment.getDestinationAccount().isBlank()
			|| payment.getSourceAccount().equalsIgnoreCase(payment.getDestinationAccount())) {
		return "INVALID_ACCOUNT";
	}

	return null;

}


private boolean hasInsufficientFunds(Payment payment, Account source) {

	if (source == null || source.getBalance() == null || payment.getCurrency() == null) {
		return false;
	}

	if (!payment.getCurrency().equalsIgnoreCase(source.getCurrency())) {
		return false;
	}

	return source.getBalance().compareTo(BigDecimal.valueOf(payment.getAmount())) < 0;

}


private void settleFunds(Payment payment, Account source) {

	if (source != null && payment.getCurrency() != null
			&& payment.getCurrency().equalsIgnoreCase(source.getCurrency())) {

		BigDecimal updatedBalance = source.getBalance().subtract(BigDecimal.valueOf(payment.getAmount()));
		source.setBalance(updatedBalance.max(BigDecimal.ZERO));
		accountRepository.update(source);
	}

	Account destination = accountRepository.findById(payment.getDestinationAccount());
	if (destination != null && payment.getCurrency() != null
			&& payment.getCurrency().equalsIgnoreCase(destination.getCurrency())) {

		BigDecimal updatedBalance = destination.getBalance().add(BigDecimal.valueOf(payment.getAmount()));
		destination.setBalance(updatedBalance);
		accountRepository.update(destination);
	}

}


private void markFailed(Payment payment, String errorCode, String note) {

	payment.setStatus("FAILED");
	payment.setErrorCode(errorCode);
	repository.update(payment);
	recordTransition(payment, "FAILED", "SYSTEM", note + " (" + errorCode + ")");

}


private void recordTransition(Payment payment, String status, String triggeredBy, String note) {

	PaymentHistory history = new PaymentHistory();
	history.setId(UUID.randomUUID().toString());
	history.setPaymentId(payment.getId());
	history.setStatus(status);
	history.setCreatedAt(LocalDateTime.now());
	history.setTriggeredBy(triggeredBy);
	history.setNote(note);

	paymentHistoryRepository.save(history);

}


private void ensureAccountExists(String accountNumber, String currency) {

	if (accountNumber == null || accountNumber.isBlank()) {
		return;
	}

	Account existing = accountRepository.findById(accountNumber);
	if (existing != null) {
		return;
	}

	Account newAccount = new Account();
	newAccount.setAccountNumber(accountNumber);
	newAccount.setAccountName("Auto " + accountNumber);
	newAccount.setBalance(SEED_BALANCE);
	newAccount.setCurrency((currency == null || currency.isBlank()) ? "USD" : currency);
	newAccount.setActive(true);

	accountRepository.save(newAccount);

}




public List<Payment> findAll(){

return repository.findAll();

}



public Payment findById(String id){

Payment payment = repository.findById(id);

if(payment == null) {
	throw new PaymentNotFoundException(
			"Payment with ID " + id + " not found");
}

return payment;

}


public int update(Payment payment) {

	return repository.update(payment);

}


public int delete(String id) {

	return repository.delete(id);

}


public PaymentSummaryDTO getSummary() {

	return repository.getPaymentSummary();

}


public List<CurrencyAmountDTO> getAmountByCurrency() {

	return repository.getAmountByCurrency();

}


}