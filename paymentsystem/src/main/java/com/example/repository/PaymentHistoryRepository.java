package com.example.repository;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Repository;

import com.example.model.PaymentHistory;

@Repository
public class PaymentHistoryRepository {

    private JdbcTemplate jdbcTemplate;

    public PaymentHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureColumnWidths() {
        // id/payment_id hold UUID.randomUUID().toString() values (36 chars) - widen
        // in case either column was originally created narrower (self-healing, ignored
        // if already wide enough or lacking privilege).
        try {
            jdbcTemplate.execute("alter table payment_history modify column id varchar(36) not null");
        } catch (Exception ex) {
            // Ignore.
        }
        try {
            jdbcTemplate.execute("alter table payment_history modify column payment_id varchar(36) not null");
        } catch (Exception ex) {
            // Ignore.
        }
    }

    public int save(PaymentHistory paymentHistory) {
        String sql = "insert into payment_history(id,payment_id,status,created_at,triggered_by,note) values(?,?,?,?,?,?)";
        try {
            return jdbcTemplate.update(sql,
                    paymentHistory.getId(),
                    paymentHistory.getPaymentId(),
                    paymentHistory.getStatus(),
                    paymentHistory.getCreatedAt(),
                    paymentHistory.getTriggeredBy(),
                    paymentHistory.getNote());
        } catch (BadSqlGrammarException ex) {
            String fallbackSql = "insert into payment_history(id,payment_id,status,`timestamp`,triggered_by,note) values(?,?,?,?,?,?)";
            return jdbcTemplate.update(fallbackSql,
                    paymentHistory.getId(),
                    paymentHistory.getPaymentId(),
                    paymentHistory.getStatus(),
                    paymentHistory.getCreatedAt(),
                    paymentHistory.getTriggeredBy(),
                    paymentHistory.getNote());
        }
    }

    public List<PaymentHistory> findAll() {
        String joinedSql = """
            select ph.*,
                   p.amount as amount,
                   p.currency as currency,
                   p.source_account as source_account,
                   p.destination_account as destination_account,
                   coalesce(a.account_number, p.source_account) as account_number
            from payment_history ph
            left join payments p on p.id = ph.payment_id
            left join accounts a on a.account_number = p.source_account
            """;

        try {
            return jdbcTemplate.query(joinedSql,
                new BeanPropertyRowMapper<>(PaymentHistory.class));
        } catch (BadSqlGrammarException ex) {
            String fallbackSql = "select * from payment_history";
            return jdbcTemplate.query(fallbackSql,
                new BeanPropertyRowMapper<>(PaymentHistory.class));
        }
    }

    public List<PaymentHistory> findLatest(int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 50));
        String mapperSql = """
            select ph.*,
                   p.amount as amount,
                   p.currency as currency,
                   p.source_account as source_account,
                   p.destination_account as destination_account,
                   coalesce(a.account_number, p.source_account) as account_number
            from payment_history ph
            left join payments p on p.id = ph.payment_id
            left join accounts a on a.account_number = p.source_account
            order by ph.created_at desc
            limit
            """ + safeLimit;

        try {
            return jdbcTemplate.query(mapperSql,
                    new BeanPropertyRowMapper<>(PaymentHistory.class));
        } catch (BadSqlGrammarException ex) {
            try {
                String fallbackSql = """
                    select ph.*,
                           p.amount as amount,
                           p.currency as currency,
                           p.source_account as source_account,
                           p.destination_account as destination_account,
                           coalesce(a.account_number, p.source_account) as account_number
                    from payment_history ph
                    left join payments p on p.id = ph.payment_id
                    left join accounts a on a.account_number = p.source_account
                    order by `timestamp` desc
                    limit
                    """ + safeLimit;
                return jdbcTemplate.query(fallbackSql,
                        new BeanPropertyRowMapper<>(PaymentHistory.class));
            } catch (BadSqlGrammarException ex2) {
                String finalFallbackSql = """
                    select ph.*,
                           p.amount as amount,
                           p.currency as currency,
                           p.source_account as source_account,
                           p.destination_account as destination_account,
                           coalesce(a.account_number, p.source_account) as account_number
                    from payment_history ph
                    left join payments p on p.id = ph.payment_id
                    left join accounts a on a.account_number = p.source_account
                    order by id desc
                    limit
                    """ + safeLimit;
                return jdbcTemplate.query(finalFallbackSql,
                        new BeanPropertyRowMapper<>(PaymentHistory.class));
            }
        }
    }

    public PaymentHistory findById(String id) {
        String joinedSql = """
            select ph.*,
                   p.amount as amount,
                   p.currency as currency,
                   p.source_account as source_account,
                   p.destination_account as destination_account,
                   coalesce(a.account_number, p.source_account) as account_number
            from payment_history ph
            left join payments p on p.id = ph.payment_id
            left join accounts a on a.account_number = p.source_account
            where ph.id=?
            """;

        List<PaymentHistory> histories;
        try {
            histories = jdbcTemplate.query(
                joinedSql,
                new BeanPropertyRowMapper<>(PaymentHistory.class),
                id);
        } catch (BadSqlGrammarException ex) {
            String fallbackSql = "select * from payment_history where id=?";
            histories = jdbcTemplate.query(
                fallbackSql,
                new BeanPropertyRowMapper<>(PaymentHistory.class),
                id);
        }

        if (histories.isEmpty()) {
            return null;
        }

        return histories.get(0);
    }

    public List<PaymentHistory> findByPaymentId(String paymentId) {
        String sql = """
            select ph.*,
                   p.amount as amount,
                   p.currency as currency,
                   p.source_account as source_account,
                   p.destination_account as destination_account,
                   coalesce(a.account_number, p.source_account) as account_number
            from payment_history ph
            left join payments p on p.id = ph.payment_id
            left join accounts a on a.account_number = p.source_account
            where ph.payment_id=?
            order by ph.created_at asc
            """;

        try {
            return jdbcTemplate.query(sql,
                    new BeanPropertyRowMapper<>(PaymentHistory.class),
                    paymentId);
        } catch (BadSqlGrammarException ex) {
            String fallbackSql = "select * from payment_history where payment_id=? order by id asc";
            return jdbcTemplate.query(fallbackSql,
                    new BeanPropertyRowMapper<>(PaymentHistory.class),
                    paymentId);
        }
    }

    public Set<String> findDistinctStatusesByPaymentId(String paymentId) {
        String sql = "select distinct upper(coalesce(status, '')) as status from payment_history where payment_id=?";
        return Set.copyOf(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("status"), paymentId));
    }

    public int update(PaymentHistory paymentHistory) {

        String sql = "update payment_history set status=?,created_at=? where id=?";

        try {
            return jdbcTemplate.update(sql,
                    paymentHistory.getStatus(),
                    paymentHistory.getCreatedAt(),
                    paymentHistory.getId());
        } catch (BadSqlGrammarException ex) {
            String fallbackSql = "update payment_history set status=?,`timestamp`=? where id=?";
            return jdbcTemplate.update(fallbackSql,
                    paymentHistory.getStatus(),
                    paymentHistory.getCreatedAt(),
                    paymentHistory.getId());
        }
    }
}
