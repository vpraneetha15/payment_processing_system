package com.example.repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.model.Payment;

@Repository
public class PaymentRepository {

    private JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(Payment payment) {

        String sql =
                "insert into payments(amount,currency,source_account,destination_account,status,created_at) values(?,?,?,?,?,?)";

        return jdbcTemplate.update(sql,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getSourceAccount(),
                payment.getDestinationAccount(),
                payment.getStatus(),
                payment.getCreatedAt());
    }

    public List<Payment> findAll() {

        String sql = "select * from payments";

        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Payment.class));
    }

    public Payment findById(String id) {

        String sql = "select * from payments where id=?";

        List<Payment> payments = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Payment.class),
                id);

        if (payments.isEmpty()) {
            return null;
        }

        return payments.get(0);
    }

    public int update(Payment payment) {

        String sql =
                "update payments set amount=?,currency=?,source_account=?,destination_account=?,status=? where id=?";

        return jdbcTemplate.update(sql,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getSourceAccount(),
                payment.getDestinationAccount(),
                payment.getStatus(),
                payment.getId());
    }

    public int delete(String id) {

        String sql = "delete from payments where id=?";

        return jdbcTemplate.update(sql, id);
    }
}