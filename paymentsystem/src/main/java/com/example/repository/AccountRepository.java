package com.example.repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.model.Account;

@Repository
public class AccountRepository {

    private JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(Account account) {

        String sql =
            "insert into accounts(account_number,account_name,balance,currency,active) values(?,?,?,?,?)";

        return jdbcTemplate.update(sql,
            account.getAccountNumber(),
            account.getAccountName(),
            account.getBalance(),
            account.getCurrency(),
            account.isActive());
    }

    public List<Account> findAll() {

        String sql = "select * from accounts";

        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Account.class));
    }

    public Account findById(String accountNumber) {

        String sql = "select * from accounts where account_number=?";

        List<Account> accounts = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Account.class),
                accountNumber);

        if (accounts.isEmpty()) {
            return null;
        }

        return accounts.get(0);
    }

    public int update(Account account) {

        String sql =
                "update accounts set account_name=?,balance=?,currency=?,active=? where account_number=?";

        return jdbcTemplate.update(sql,
                account.getAccountName(),
                account.getBalance(),
                account.getCurrency(),
                account.isActive(),
                account.getAccountNumber());
    }

    public int delete(String accountNumber) {

        String sql = "delete from accounts where account_number=?";

        return jdbcTemplate.update(sql, accountNumber);
    }
}