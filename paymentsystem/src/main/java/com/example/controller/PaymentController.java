package com.example.controller;


import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;


import com.example.model.Payment;
import com.example.service.PaymentService;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/payments")
@Validated
@CrossOrigin(origins = "*")
public class PaymentController {



private PaymentService service;



public PaymentController(PaymentService service){

this.service=service;

}



@PostMapping
public String save(
@Valid @RequestBody Payment payment){

service.save(payment);

return "Payment Saved";

}




@GetMapping
public List<Payment> getPayments() {

return service.findAll();

}




@GetMapping("/{id}")
public Payment getPayment(@PathVariable String id) {

return service.findById(id);

}

@PutMapping
public String update(@Valid @RequestBody Payment payment) {

service.update(payment);

return "Updated Successfully";

}

@DeleteMapping("/{id}")
public String delete(@PathVariable String id) {

service.delete(id);

return "Deleted Successfully";

}


}