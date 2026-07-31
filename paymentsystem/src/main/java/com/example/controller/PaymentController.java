package com.example.controller;


import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;


import com.example.model.Payment;
import com.example.service.PaymentService;



@RestController
@RequestMapping("/payments")
@Validated
@CrossOrigin(origins = "*")
public class PaymentController {



private final PaymentService service;



public PaymentController(PaymentService service){

this.service=service;

}



@PostMapping
<<<<<<< HEAD
public ResponseEntity<?> save(
@Valid @RequestBody Payment payment){
=======
public String save(
@RequestBody Payment payment){
>>>>>>> fad0cf3787d2ee47429c719891e58fb1b95dd7f6

try {
service.save(payment);
return ResponseEntity.status(HttpStatus.CREATED).body(payment);
} catch (DataIntegrityViolationException ex) {
return ResponseEntity.badRequest().body(
	"Invalid account details. Ensure source and destination account numbers exist.");
}

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