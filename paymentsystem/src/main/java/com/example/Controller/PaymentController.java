package com.example.paymentsystem.controller;


import java.util.List;

import org.springframework.web.bind.annotation.*;


import com.example.paymentsystem.model.Payment;
import com.example.paymentsystem.service.PaymentService;



@RestController
@RequestMapping("/payments")
@CrossOrigin("*")
public class PaymentController {



private final PaymentService service;



public PaymentController(PaymentService service){

this.service=service;

}



@PostMapping
public Payment createPayment(
@RequestBody Payment payment){

return service.createPayment(payment);

}




@GetMapping
public List<Payment> getPayments(){

return service.getPayments();

}




@GetMapping("/{id}")
public Payment getPayment(
@PathVariable Long id){

return service.getPayment(id);

}


}