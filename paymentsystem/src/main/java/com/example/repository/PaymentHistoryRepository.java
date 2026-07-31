package com.example.repository;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.model.PaymentHistory;

@Repository
public class PaymentHistoryRepository {

    private JdbcTemplate jdbcTemplate;

    public PaymentHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(PaymentHistory paymentHistory) {

        String sql =
                "insert into payment_history(payment_id,status,created_at) values(?,?,?)";

        return jdbcTemplate.update(sql,
                paymentHistory.getPaymentId(),
                paymentHistory.getStatus(),
                paymentHistory.getCreatedAt());
    }

    public List<PaymentHistory> findAll() {

        String sql = "select * from payment_history";

        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(PaymentHistory.class));
    }

    public PaymentHistory findById(String id) {

        String sql = "select * from payment_history where id=?";

        List<PaymentHistory> histories = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(PaymentHistory.class),
                id);

        if (histories.isEmpty()) {
            return null;
        }

        return histories.get(0);
    }

    public int update(PaymentHistory paymentHistory) {

        String sql = "update payment_history set status=?,created_at=? where id=?";

        return jdbcTemplate.update(sql,
                paymentHistory.getStatus(),
                paymentHistory.getCreatedAt(),
                paymentHistory.getId());
    }
}
