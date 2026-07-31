package com.example.controller;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Account;
import com.example.service.AccountService;

@RestController
@RequestMapping("/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

	private AccountService service;

	public AccountController(AccountService service) {
		this.service = service;
	}

	@PostMapping
	public String save(@RequestBody Account account) {

		service.save(account);

		return "Account Saved";
	}

	@GetMapping
	public List<Account> getAccounts() {

		return service.findAll();
	}

	@GetMapping("/{accountNumber}")
	public Account getAccount(@PathVariable String accountNumber) {

		return service.findById(accountNumber);
	}

	@PutMapping
	public String update(@RequestBody Account account) {

		service.update(account);

		return "Updated Successfully";
	}

	@DeleteMapping("/{accountNumber}")
	public String delete(@PathVariable String accountNumber) {

		service.delete(accountNumber);

		return "Deleted Successfully";
	}
}
