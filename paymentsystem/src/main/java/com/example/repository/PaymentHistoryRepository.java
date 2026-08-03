package com.example.repository;
import java.util.List;
import java.util.Set;

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
                   (
                       select coalesce(a.account_number, p.source_account)
                       from payments p
                       left join accounts a on a.account_number = p.source_account
                       where p.id = ph.payment_id
                       limit 1
                   ) as account_number
            from payment_history ph
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
                   (
                       select coalesce(a.account_number, p.source_account)
                       from payments p
                       left join accounts a on a.account_number = p.source_account
                       where p.id = ph.payment_id
                       limit 1
                   ) as account_number
            from payment_history ph
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
                           (
                               select coalesce(a.account_number, p.source_account)
                               from payments p
                               left join accounts a on a.account_number = p.source_account
                               where p.id = ph.payment_id
                               limit 1
                           ) as account_number
                    from payment_history ph
                    order by `timestamp` desc
                    limit
                    """ + safeLimit;
                return jdbcTemplate.query(fallbackSql,
                        new BeanPropertyRowMapper<>(PaymentHistory.class));
            } catch (BadSqlGrammarException ex2) {
                String finalFallbackSql = """
                    select ph.*,
                           (
                               select coalesce(a.account_number, p.source_account)
                               from payments p
                               left join accounts a on a.account_number = p.source_account
                               where p.id = ph.payment_id
                               limit 1
                           ) as account_number
                    from payment_history ph
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
                   (
                       select coalesce(a.account_number, p.source_account)
                       from payments p
                       left join accounts a on a.account_number = p.source_account
                       where p.id = ph.payment_id
                       limit 1
                   ) as account_number
            from payment_history ph
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
                   (
                       select coalesce(a.account_number, p.source_account)
                       from payments p
                       left join accounts a on a.account_number = p.source_account
                       where p.id = ph.payment_id
                       limit 1
                   ) as account_number
            from payment_history ph
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
