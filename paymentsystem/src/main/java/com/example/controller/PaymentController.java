package com.example.controller;


import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;


import com.example.dto.CurrencyAmountDTO;
import com.example.dto.PaymentSummaryDTO;
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
public ResponseEntity<?> save(
@RequestBody Payment payment){

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


@GetMapping("/summary")
public PaymentSummaryDTO getPaymentSummary(){

return service.getSummary();

}


@GetMapping("/amount-by-currency")
public List<CurrencyAmountDTO> getAmountByCurrency(){

return service.getAmountByCurrency();
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