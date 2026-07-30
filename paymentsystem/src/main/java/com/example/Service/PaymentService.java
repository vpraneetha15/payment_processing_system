package com.example.service;


import java.time.LocalDateTime;
import java.util.List;


import org.springframework.stereotype.Service;

import com.example.paymentsystem.model.Payment;
import com.example.paymentsystem.repository.PaymentRepository;



@Service
public class PaymentService {


private final PaymentRepository repository;


public PaymentService(PaymentRepository repository){

this.repository=repository;

}



public Payment createPayment(Payment payment){


payment.setStatus("CREATED");

payment.setCreatedAt(LocalDateTime.now());


return repository.save(payment);

}




public List<Payment> getPayments(){

return repository.findAll();

}



public Payment getPayment(Long id){

return repository.findById(id)
.orElse(null);

}


}