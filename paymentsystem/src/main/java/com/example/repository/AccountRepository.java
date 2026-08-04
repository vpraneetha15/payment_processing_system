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
            account.getBalance() != null
                    ? account.getBalance().setScale(2, java.math.RoundingMode.HALF_UP)
                    : java.math.BigDecimal.ZERO,
            account.getCurrency(),
            account.isActive());
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

    /**
     * Returns the account_number linked to the given card number (from user_cards),
     * or null if not found / card is inactive.
     */
    public String findAccountNumberByCardNumber(String cardNumber) {
        String sql = "select account_number from cards where card_number = ? and active = true limit 1";
        List<String> rows = jdbcTemplate.queryForList(sql, String.class, cardNumber);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Returns the account_number linked to the given UPI ID (from user_upi),
     * or null if not found / UPI is inactive.
     */
    public String findAccountNumberByUpiId(String upiId) {
        String sql = "select account_number from upi_accounts where upi_id = ? and active = true limit 1";
        List<String> rows = jdbcTemplate.queryForList(sql, String.class, upiId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Returns the account_number whose mobile_number matches (from accounts),
     * or null if not found / account is inactive.
     */
    public String findAccountNumberByMobile(String mobileNumber) {
        String sql = "select account_number from accounts where mobile_number = ? and active = true limit 1";
        List<String> rows = jdbcTemplate.queryForList(sql, String.class, mobileNumber);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Returns the email address linked to an account number, or null if the
     * account doesn't exist or has no email column.
     */
    public String findEmailByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) return null;
        try {
            String sql = "select email from accounts where account_number = ? limit 1";
            List<String> rows = jdbcTemplate.queryForList(sql, String.class, accountNumber);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            // email column may not exist in older schema versions
            return null;
        }
    }
}