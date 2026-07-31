package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.Account;
import com.example.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public int save(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findById(String accountNumber) {
        return accountRepository.findById(accountNumber);
    }

    public int update(Account account) {
        return accountRepository.update(account);
    }

    public int delete(String accountNumber) {
        return accountRepository.delete(accountNumber);
    }
}