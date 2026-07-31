package com.example.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import org.springframework.stereotype.Service;

import com.example.dto.PaymentDTO;
import com.example.exception.PaymentNotFoundException;
import com.example.model.Account;
import com.example.model.Payment;
import com.example.model.PaymentHistory;
import com.example.repository.AccountRepository;
import com.example.repository.PaymentHistoryRepository;
import com.example.repository.PaymentRepository;



@Service
public class PaymentService {


private PaymentRepository repository;
private AccountRepository accountRepository;
private PaymentHistoryRepository paymentHistoryRepository;


public PaymentService(PaymentRepository repository,
	AccountRepository accountRepository,
	PaymentHistoryRepository paymentHistoryRepository){

this.repository=repository;
this.accountRepository=accountRepository;
this.paymentHistoryRepository=paymentHistoryRepository;

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
	newAccount.setBalance(BigDecimal.ZERO);
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


}