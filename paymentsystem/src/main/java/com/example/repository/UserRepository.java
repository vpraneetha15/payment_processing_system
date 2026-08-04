package com.example.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.model.User;
import com.example.model.UserCard;
import com.example.model.UserWallet;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureAuxiliaryTables() {
        try {
            jdbcTemplate.execute("""
                    create table if not exists user_upi (
                        account_number varchar(30) primary key,
                        upi_id varchar(120) not null unique,
                        status varchar(20) not null default 'ACTIVE',
                        created_at timestamp default current_timestamp,
                        constraint fk_upi_account foreign key (account_number)
                            references accounts(account_number)
                            on delete cascade
                    )
                    """);
        } catch (Exception ignored) {
        }

        try {
            jdbcTemplate.execute("""
                    create table if not exists user_cards (
                        id bigint primary key auto_increment,
                        account_number varchar(30) not null,
                        card_number varchar(16) not null unique,
                        card_type varchar(20) not null,
                        card_balance decimal(12,2) not null,
                        active boolean not null default true,
                        created_at timestamp default current_timestamp,
                        constraint fk_card_account foreign key (account_number)
                            references accounts(account_number)
                            on delete cascade
                    )
                    """);
        } catch (Exception ignored) {
        }

        try {
            jdbcTemplate.execute("""
                    create table if not exists user_wallets (
                        id bigint primary key auto_increment,
                        account_number varchar(30) not null,
                        wallet_provider varchar(40) not null,
                        wallet_id varchar(120) not null unique,
                        active boolean not null default true,
                        created_at timestamp default current_timestamp,
                        constraint fk_wallet_account foreign key (account_number)
                            references accounts(account_number)
                            on delete cascade
                    )
                    """);
        } catch (Exception ignored) {
        }
    }

    public List<User> findAllUsers() {
        String sqlWithUpiAndOptionalColumns = """
                select a.account_number as accountNumber,
                       a.account_name as fullName,
                       a.email as email,
                       a.mobile_number as mobileNumber,
                       a.balance as openingBalance,
                       a.currency as preferredCurrency,
                       a.notes as notes,
                       a.active as active,
                       a.created_at as createdAt,
                       u.upi_id as upiId,
                       u.status as upiStatus
                from accounts a
                left join user_upi u on u.account_number = a.account_number
                order by a.created_at desc
                """;

        try {
            return jdbcTemplate.query(sqlWithUpiAndOptionalColumns, new BeanPropertyRowMapper<>(User.class));
        } catch (Exception ex) {
            String fallbackWithUpiWithCreatedAt = """
                    select a.account_number as accountNumber,
                           a.account_name as fullName,
                           null as email,
                           null as mobileNumber,
                           a.balance as openingBalance,
                           a.currency as preferredCurrency,
                           null as notes,
                           a.active as active,
                           a.created_at as createdAt,
                           u.upi_id as upiId,
                           u.status as upiStatus
                    from accounts a
                    left join user_upi u on u.account_number = a.account_number
                    order by a.created_at desc
                    """;
            try {
                return jdbcTemplate.query(fallbackWithUpiWithCreatedAt, new BeanPropertyRowMapper<>(User.class));
            } catch (Exception ignored) {
                String fallbackWithUpiNoCreatedAt = """
                        select a.account_number as accountNumber,
                               a.account_name as fullName,
                               null as email,
                               null as mobileNumber,
                               a.balance as openingBalance,
                               a.currency as preferredCurrency,
                               null as notes,
                               a.active as active,
                               null as createdAt,
                               u.upi_id as upiId,
                               u.status as upiStatus
                        from accounts a
                        left join user_upi u on u.account_number = a.account_number
                        order by a.account_number desc
                        """;
                try {
                    return jdbcTemplate.query(fallbackWithUpiNoCreatedAt, new BeanPropertyRowMapper<>(User.class));
                } catch (Exception ignoredAgain) {
                    String fallbackAccountsOnlyWithCreatedAt = """
                            select a.account_number as accountNumber,
                                   a.account_name as fullName,
                                   a.balance as openingBalance,
                                   a.currency as preferredCurrency,
                                   a.active as active,
                                   a.created_at as createdAt
                            from accounts a
                            order by a.created_at desc
                            """;
                    try {
                        return jdbcTemplate.query(fallbackAccountsOnlyWithCreatedAt, new BeanPropertyRowMapper<>(User.class));
                    } catch (Exception ignoredThird) {
                        String fallbackAccountsOnly = """
                        select a.account_number as accountNumber,
                               a.account_name as fullName,
                               a.balance as openingBalance,
                               a.currency as preferredCurrency,
                               a.active as active
                        from accounts a
                        order by a.account_number desc
                        """;
                        try {
                            return jdbcTemplate.query(fallbackAccountsOnly, new BeanPropertyRowMapper<>(User.class));
                        } catch (Exception lastFailure) {
                            return Collections.emptyList();
                        }
                    }
                }
            }
        }
    }

    public User findUserByAccountNumber(String accountNumber) {
        String sqlWithUpiAndOptionalColumns = """
                select a.account_number as accountNumber,
                       a.account_name as fullName,
                       a.email as email,
                       a.mobile_number as mobileNumber,
                       a.balance as openingBalance,
                       a.currency as preferredCurrency,
                       a.notes as notes,
                       a.active as active,
                       a.created_at as createdAt,
                       u.upi_id as upiId,
                       u.status as upiStatus
                from accounts a
                left join user_upi u on u.account_number = a.account_number
                where a.account_number = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sqlWithUpiAndOptionalColumns, new BeanPropertyRowMapper<>(User.class), accountNumber);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        } catch (Exception ex) {
            String fallbackWithUpiWithCreatedAt = """
                    select a.account_number as accountNumber,
                           a.account_name as fullName,
                           null as email,
                           null as mobileNumber,
                           a.balance as openingBalance,
                           a.currency as preferredCurrency,
                           null as notes,
                           a.active as active,
                           a.created_at as createdAt,
                           u.upi_id as upiId,
                           u.status as upiStatus
                    from accounts a
                    left join user_upi u on u.account_number = a.account_number
                    where a.account_number = ?
                    """;
            try {
                return jdbcTemplate.queryForObject(fallbackWithUpiWithCreatedAt, new BeanPropertyRowMapper<>(User.class), accountNumber);
            } catch (Exception ignored) {
                String fallbackWithUpiNoCreatedAt = """
                        select a.account_number as accountNumber,
                               a.account_name as fullName,
                               null as email,
                               null as mobileNumber,
                               a.balance as openingBalance,
                               a.currency as preferredCurrency,
                               null as notes,
                               a.active as active,
                               null as createdAt,
                               u.upi_id as upiId,
                               u.status as upiStatus
                        from accounts a
                        left join user_upi u on u.account_number = a.account_number
                        where a.account_number = ?
                        """;
                try {
                    return jdbcTemplate.queryForObject(fallbackWithUpiNoCreatedAt, new BeanPropertyRowMapper<>(User.class), accountNumber);
                } catch (Exception ignoredAgain) {
                    String fallbackAccountsOnlyWithCreatedAt = """
                        select a.account_number as accountNumber,
                               a.account_name as fullName,
                               a.balance as openingBalance,
                               a.currency as preferredCurrency,
                               a.active as active,
                               a.created_at as createdAt
                        from accounts a
                        where a.account_number = ?
                        """;
                    try {
                        return jdbcTemplate.queryForObject(fallbackAccountsOnlyWithCreatedAt, new BeanPropertyRowMapper<>(User.class), accountNumber);
                    } catch (Exception ignoredThird) {
                        String fallbackAccountsOnly = """
                        select a.account_number as accountNumber,
                               a.account_name as fullName,
                               a.balance as openingBalance,
                               a.currency as preferredCurrency,
                               a.active as active
                        from accounts a
                        where a.account_number = ?
                        """;
                        try {
                            return jdbcTemplate.queryForObject(fallbackAccountsOnly, new BeanPropertyRowMapper<>(User.class), accountNumber);
                        } catch (EmptyResultDataAccessException notFound) {
                            return null;
                        } catch (Exception finalFailure) {
                            return null;
                        }
                    }
                }
            }
        }
    }

    public boolean existsEmail(String email, String excludeAccountNumber) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "select count(*) from accounts where lower(email) = lower(?)";
        String withExclude = sql + " and account_number <> ?";

        try {
            Integer count = excludeAccountNumber == null
                    ? jdbcTemplate.queryForObject(sql, Integer.class, email)
                    : jdbcTemplate.queryForObject(withExclude, Integer.class, email, excludeAccountNumber);
            return count != null && count > 0;
        } catch (BadSqlGrammarException ex) {
            return false;
        }
    }

    public boolean existsMobile(String mobileNumber, String excludeAccountNumber) {
        if (mobileNumber == null || mobileNumber.isBlank()) {
            return false;
        }

        String sql = "select count(*) from accounts where mobile_number = ?";
        String withExclude = sql + " and account_number <> ?";

        try {
            Integer count = excludeAccountNumber == null
                    ? jdbcTemplate.queryForObject(sql, Integer.class, mobileNumber)
                    : jdbcTemplate.queryForObject(withExclude, Integer.class, mobileNumber, excludeAccountNumber);
            return count != null && count > 0;
        } catch (BadSqlGrammarException ex) {
            return false;
        }
    }

    public String generateNextAccountNumber() {
        String sql = "select account_number from accounts where account_number like 'ACC%'";
        List<String> accountNumbers = jdbcTemplate.queryForList(sql, String.class);

        int max = 0;
        for (String accountNumber : accountNumbers) {
            if (accountNumber == null || !accountNumber.matches("^ACC\\d+$")) {
                continue;
            }
            int value = Integer.parseInt(accountNumber.substring(3));
            max = Math.max(max, value);
        }

        return String.format("ACC%03d", max + 1);
    }

    public int insertUser(User user) {
        String sql = """
                insert into accounts(account_number, account_name, email, mobile_number, balance, currency, notes, active, created_at)
                values(?,?,?,?,?,?,?,?,?)
                """;

        try {
            return jdbcTemplate.update(sql,
                    user.getAccountNumber(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getMobileNumber(),
                    user.getOpeningBalance(),
                    user.getPreferredCurrency(),
                    user.getNotes(),
                    user.isActive(),
                    Timestamp.valueOf(user.getCreatedAt()));
        } catch (BadSqlGrammarException ex) {
            String fallbackSql = "insert into accounts(account_number,account_name,balance,currency,active) values(?,?,?,?,?)";
            return jdbcTemplate.update(fallbackSql,
                    user.getAccountNumber(),
                    user.getFullName(),
                    user.getOpeningBalance(),
                    user.getPreferredCurrency(),
                    user.isActive());
        }
    }

    public int updateUser(String accountNumber, String fullName, String email, String mobileNumber,
                          String preferredCurrency, String notes, boolean active) {

        String sql = """
                update accounts
                set account_name = ?,
                    email = ?,
                    mobile_number = ?,
                    currency = ?,
                    notes = ?,
                    active = ?
                where account_number = ?
                """;

        try {
            return jdbcTemplate.update(sql,
                    fullName,
                    email,
                    mobileNumber,
                    preferredCurrency,
                    notes,
                    active,
                    accountNumber);
        } catch (BadSqlGrammarException ex) {
            String fallbackSql = "update accounts set account_name=?,currency=?,active=? where account_number=?";
            return jdbcTemplate.update(fallbackSql,
                    fullName,
                    preferredCurrency,
                    active,
                    accountNumber);
        }
    }

    public int updateUserStatus(String accountNumber, boolean active) {
        String sql = "update accounts set active=? where account_number=?";
        return jdbcTemplate.update(sql, active, accountNumber);
    }

    public int insertUpi(String accountNumber, String upiId, String status, LocalDateTime createdAt) {
        String sql = """
                insert into user_upi(account_number, upi_id, status, created_at)
                values(?,?,?,?)
                """;

        return jdbcTemplate.update(sql, accountNumber, upiId, status, Timestamp.valueOf(createdAt));
    }

    public boolean existsCardNumber(String cardNumber) {
        String sql = "select count(*) from user_cards where card_number = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cardNumber);
        return count != null && count > 0;
    }

    public int insertCard(String accountNumber, String cardNumber, String cardType, BigDecimal cardBalance, boolean active) {
        String sql = """
                insert into user_cards(account_number, card_number, card_type, card_balance, active)
                values(?,?,?,?,?)
                """;

        return jdbcTemplate.update(sql, accountNumber, cardNumber, cardType, cardBalance, active);
    }

    public List<UserCard> findCardsByAccount(String accountNumber) {
        String sql = """
                select id,
                       account_number as accountNumber,
                       card_number as cardNumber,
                       card_type as cardType,
                       card_balance as cardBalance,
                       active,
                       created_at as createdAt
                from user_cards
                where account_number = ?
                order by created_at desc
                """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserCard.class), accountNumber);
    }

    public boolean existsWalletId(String walletId) {
        String sql = "select count(*) from user_wallets where wallet_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, walletId);
        return count != null && count > 0;
    }

    public int insertWallet(String accountNumber, String walletProvider, String walletId, boolean active) {
        String sql = """
                insert into user_wallets(account_number, wallet_provider, wallet_id, active)
                values(?,?,?,?)
                """;

        return jdbcTemplate.update(sql, accountNumber, walletProvider, walletId, active);
    }

    public List<UserWallet> findWalletsByAccount(String accountNumber) {
        String sql = """
                select id,
                       account_number as accountNumber,
                       wallet_provider as walletProvider,
                       wallet_id as walletId,
                       active,
                       created_at as createdAt
                from user_wallets
                where account_number = ?
                order by created_at desc
                """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserWallet.class), accountNumber);
    }
}
