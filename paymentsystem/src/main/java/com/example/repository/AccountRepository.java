package com.example.repository;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
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

    @PostConstruct
    public void ensureEmailColumn() {
        try {
            jdbcTemplate.execute("alter table accounts add column email varchar(255) null");
        } catch (Exception ex) {
            // Column likely already exists, or the DB user lacks DDL privileges.
        }
    }

    public int save(Account account) {

        String sql =
            "insert into accounts(account_number,account_name,balance,currency,email,active) values(?,?,?,?,?,?)";

        try {
            return jdbcTemplate.update(sql,
                account.getAccountNumber(),
                account.getAccountName(),
                account.getBalance(),
                account.getCurrency(),
                account.getEmail(),
                account.isActive());
        } catch (BadSqlGrammarException ex) {
            String fallbackSql =
                "insert into accounts(account_number,account_name,balance,currency,active) values(?,?,?,?,?)";
            return jdbcTemplate.update(fallbackSql,
                account.getAccountNumber(),
                account.getAccountName(),
                account.getBalance(),
                account.getCurrency(),
                account.isActive());
        }
    }

    public List<Account> findAll() {

        String sql = "select * from accounts order by created_at desc";

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
                "update accounts set account_name=?,balance=?,currency=?,email=?,active=? where account_number=?";

        try {
            return jdbcTemplate.update(sql,
                    account.getAccountName(),
                    account.getBalance(),
                    account.getCurrency(),
                    account.getEmail(),
                    account.isActive(),
                    account.getAccountNumber());
        } catch (BadSqlGrammarException ex) {
            String fallbackSql =
                    "update accounts set account_name=?,balance=?,currency=?,active=? where account_number=?";
            return jdbcTemplate.update(fallbackSql,
                    account.getAccountName(),
                    account.getBalance(),
                    account.getCurrency(),
                    account.isActive(),
                    account.getAccountNumber());
        }
    }

    public String findEmailByAccountNumber(String accountNumber) {
        String sql = "select email from accounts where account_number=?";
        try {
            return jdbcTemplate.query(sql, rs -> {
                if (!rs.next()) {
                    return null;
                }
                String email = rs.getString("email");
                return (email == null || email.isBlank()) ? null : email;
            }, accountNumber);
        } catch (DataAccessException ex) {
            // Accounts table or email column not available in this environment.
            return null;
        }
    }

    public int delete(String accountNumber) {

        String sql = "delete from accounts where account_number=?";

        return jdbcTemplate.update(sql, accountNumber);
    }
}