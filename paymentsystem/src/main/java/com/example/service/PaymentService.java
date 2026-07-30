package com.example.service;


import java.time.LocalDateTime;
import java.util.List;


import org.springframework.stereotype.Service;

import com.example.dto.PaymentDTO;
import com.example.exception.PaymentNotFoundException;
import com.example.model.Payment;
import com.example.repository.PaymentRepository;



@Service
public class PaymentService {


private PaymentRepository repository;


public PaymentService(PaymentRepository repository){

this.repository=repository;

}



public int save(PaymentDTO dto) {

Payment payment = new Payment();

payment.setAmount(dto.getAmount());
payment.setCurrency(dto.getCurrency());
payment.setSourceAccount(dto.getSourceAccount());
payment.setDestinationAccount(dto.getDestinationAccount());

payment.setStatus("CREATED");
payment.setCreatedAt(LocalDateTime.now());

return repository.save(payment);

}


public int save(Payment payment) {

	if (payment.getStatus() == null || payment.getStatus().isBlank()) {
		payment.setStatus("CREATED");
	}
	if (payment.getCreatedAt() == null) {
		payment.setCreatedAt(LocalDateTime.now());
	}

	return repository.save(payment);

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