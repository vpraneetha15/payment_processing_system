package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.Account;
import com.example.repository.AccountRepository;

@Service
public class AccountService {

	private AccountRepository repository;

	public AccountService(AccountRepository repository) {
		this.repository = repository;
	}

	public int save(Account account) {

		return repository.save(account);
	}

	public List<Account> findAll() {

		return repository.findAll();
	}

	public Account findById(String accountNumber) {

		return repository.findById(accountNumber);
	}

	public int update(Account account) {

		return repository.update(account);
	}

	public int delete(String accountNumber) {

		return repository.delete(accountNumber);
	}
}
