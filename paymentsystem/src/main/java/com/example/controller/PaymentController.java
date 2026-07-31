package com.example.controller;


import java.util.List;

import org.springframework.web.bind.annotation.*;


import com.example.model.Payment;
import com.example.service.PaymentService;



@RestController
@RequestMapping("/payments")
@CrossOrigin("*")
public class PaymentController {



private final PaymentService service;



public PaymentController(PaymentService service){

this.service=service;

}



@PostMapping
public String save(
@RequestBody Payment payment){

service.save(payment);

return "Payment Saved";

}




@GetMapping
public List<Payment> getPayments(){

return service.findAll();

}




@GetMapping("/{id}")
public Payment getPayment(
@PathVariable String id){

return service.findById(id);

}


@PutMapping
public String update(@RequestBody Payment payment) {

service.update(payment);

return "Updated Successfully";

}


@DeleteMapping("/{id}")
public String delete(@PathVariable String id) {

service.delete(id);

return "Deleted Successfully";

}


}