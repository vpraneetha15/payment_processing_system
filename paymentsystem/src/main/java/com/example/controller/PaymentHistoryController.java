package com.example.controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.PaymentHistory;
import com.example.service.PaymentHistoryService;

@RestController
@RequestMapping("/payment-history")
@CrossOrigin(origins = "*")
public class PaymentHistoryController {

	private PaymentHistoryService service;

	public PaymentHistoryController(PaymentHistoryService service) {
		this.service = service;
	}

	@PostMapping
	public String save(@RequestBody PaymentHistory paymentHistory) {

		service.save(paymentHistory);

		return "Payment History Saved";
	}

	@GetMapping
	public List<PaymentHistory> getPaymentHistory() {

		return service.findAll();
	}

	@GetMapping("/{id}")
	public PaymentHistory getHistory(@PathVariable String id) {

		return service.findById(id);
	}

	@PutMapping
	public String update(@RequestBody PaymentHistory paymentHistory) {

		service.update(paymentHistory);

		return "Updated Successfully";
	}

}
