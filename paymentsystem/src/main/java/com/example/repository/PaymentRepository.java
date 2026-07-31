package com.example.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.dto.CurrencyAmountDTO;
import com.example.dto.PaymentSummaryDTO;
import com.example.model.Payment;

@Repository
public class PaymentRepository {

    private JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureErrorCodeColumn() {
        try {
            jdbcTemplate.execute("alter table payments add column error_code varchar(50) null");
        } catch (Exception ex) {
            // Column likely already exists, or the DB user lacks DDL privileges.
            // Analytics queries fall back gracefully when the column is missing.
        }
    }

    public int save(Payment payment) {

        String sql =
            "insert into payments(id,amount,currency,source_account,destination_account,status,error_code,created_at) values(?,?,?,?,?,?,?,?)";

        try {
            return jdbcTemplate.update(sql,
                payment.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getSourceAccount(),
                    payment.getDestinationAccount(),
                    payment.getStatus(),
                    payment.getErrorCode(),
                    payment.getCreatedAt());
        } catch (BadSqlGrammarException ex) {
            String fallbackSql =
                "insert into payments(id,amount,currency,source_account,destination_account,status,created_at) values(?,?,?,?,?,?,?)";
            return jdbcTemplate.update(fallbackSql,
                payment.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getSourceAccount(),
                    payment.getDestinationAccount(),
                    payment.getStatus(),
                    payment.getCreatedAt());
        }
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
                "update payments set amount=?,currency=?,source_account=?,destination_account=?,status=?,error_code=? where id=?";

        try {
            return jdbcTemplate.update(sql,
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getSourceAccount(),
                    payment.getDestinationAccount(),
                    payment.getStatus(),
                    payment.getErrorCode(),
                    payment.getId());
        } catch (BadSqlGrammarException ex) {
            String fallbackSql =
                    "update payments set amount=?,currency=?,source_account=?,destination_account=?,status=? where id=?";
            return jdbcTemplate.update(fallbackSql,
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getSourceAccount(),
                    payment.getDestinationAccount(),
                    payment.getStatus(),
                    payment.getId());
        }
    }

    public int delete(String id) {

        String sql = "delete from payments where id=?";

        return jdbcTemplate.update(sql, id);
    }

    public PaymentSummaryDTO getPaymentSummary() {

        String sql = """
                select
                    count(*) as total_payments,
                    coalesce(sum(amount), 0) as total_amount,
                    sum(case when upper(coalesce(status, '')) in ('SUCCESS','SUCCEEDED','COMPLETED') then 1 else 0 end) as success_count,
                    sum(case when upper(coalesce(status, '')) in ('PENDING','CREATED','PROCESSING','ON_HOLD') then 1 else 0 end) as pending_count,
                    sum(case when upper(coalesce(status, '')) in ('FAILED','REJECTED','CANCELLED') then 1 else 0 end) as failed_count
                from payments
                """;

        Map<String, Object> row = jdbcTemplate.queryForMap(sql);

        String currencySql = """
                select coalesce(group_concat(distinct upper(currency) order by upper(currency) separator ', '), '') as currencies
                from payments
                """;

        String currencies = jdbcTemplate.queryForObject(currencySql, String.class);

        PaymentSummaryDTO summary = new PaymentSummaryDTO();
        summary.setTotalPayments(toLong(row.get("total_payments")));
        summary.setTotalAmount(toDouble(row.get("total_amount")));
        summary.setSuccessCount(toLong(row.get("success_count")));
        summary.setPendingCount(toLong(row.get("pending_count")));
        summary.setFailedCount(toLong(row.get("failed_count")));
        summary.setCurrencies(currencies == null ? "" : currencies);

        return summary;
    }

    public List<CurrencyAmountDTO> getAmountByCurrency() {

        String sql = """
                select upper(coalesce(currency, 'UNKNOWN')) as currency,
                       coalesce(sum(amount), 0) as amount
                from payments
                group by upper(coalesce(currency, 'UNKNOWN'))
                order by currency
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CurrencyAmountDTO(
                        rs.getString("currency"),
                        rs.getDouble("amount")));
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
}