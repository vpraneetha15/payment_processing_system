package com.example.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.dto.CurrencyVolumeDTO;
import com.example.dto.ErrorCodeCountDTO;
import com.example.dto.PaymentFilter;
import com.example.dto.StatusCountDTO;
import com.example.dto.TrendPointDTO;
import com.example.model.Payment;

@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String buildWhereClause(PaymentFilter filter, List<Object> params) {

        StringBuilder where = new StringBuilder(" where 1=1 ");

        if (filter == null) {
            return where.toString();
        }

        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            where.append(" and upper(coalesce(status,'')) in (")
                 .append(placeholders(filter.getStatuses().size()))
                 .append(") ");
            filter.getStatuses().forEach(status -> params.add(status.toUpperCase()));
        }

        if (filter.getCurrencies() != null && !filter.getCurrencies().isEmpty()) {
            where.append(" and upper(coalesce(currency,'')) in (")
                 .append(placeholders(filter.getCurrencies().size()))
                 .append(") ");
            filter.getCurrencies().forEach(currency -> params.add(currency.toUpperCase()));
        }

        if (filter.getErrorCodes() != null && !filter.getErrorCodes().isEmpty()) {
            where.append(" and upper(coalesce(error_code,'')) in (")
                 .append(placeholders(filter.getErrorCodes().size()))
                 .append(") ");
            filter.getErrorCodes().forEach(code -> params.add(code.toUpperCase()));
        }

        if (filter.getFromDate() != null) {
            where.append(" and created_at >= ? ");
            params.add(filter.getFromDate());
        }

        if (filter.getToDate() != null) {
            where.append(" and created_at <= ? ");
            params.add(filter.getToDate());
        }

        if (filter.getMinAmount() != null) {
            where.append(" and amount >= ? ");
            params.add(filter.getMinAmount());
        }

        if (filter.getMaxAmount() != null) {
            where.append(" and amount <= ? ");
            params.add(filter.getMaxAmount());
        }

        return where.toString();
    }

    private String placeholders(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(i == 0 ? "?" : ",?");
        }
        return builder.toString();
    }

    public List<StatusCountDTO> getStatusDistribution(PaymentFilter filter) {

        List<Object> params = new ArrayList<>();
        String where = buildWhereClause(filter, params);

        String sql = "select upper(coalesce(status,'UNKNOWN')) as status, count(*) as cnt "
                + "from payments " + where
                + " group by upper(coalesce(status,'UNKNOWN')) order by cnt desc";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new StatusCountDTO(rs.getString("status"), rs.getLong("cnt")),
                params.toArray());
    }

    public List<ErrorCodeCountDTO> getErrorCodeBreakdown(PaymentFilter filter) {

        List<Object> params = new ArrayList<>();
        String where = buildWhereClause(filter, params);

        String sql = "select upper(error_code) as error_code, count(*) as cnt "
                + "from payments " + where
                + " and error_code is not null and error_code <> '' "
                + "group by upper(error_code) order by cnt desc";

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) ->
                    new ErrorCodeCountDTO(rs.getString("error_code"), rs.getLong("cnt")),
                    params.toArray());
        } catch (BadSqlGrammarException ex) {
            return new ArrayList<>();
        }
    }

    public List<CurrencyVolumeDTO> getCurrencyVolume(PaymentFilter filter) {

        List<Object> params = new ArrayList<>();
        String where = buildWhereClause(filter, params);

        String sql = "select upper(coalesce(currency,'UNKNOWN')) as currency, count(*) as cnt, "
                + "coalesce(sum(amount),0) as total "
                + "from payments " + where
                + " group by upper(coalesce(currency,'UNKNOWN')) order by total desc";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new CurrencyVolumeDTO(rs.getString("currency"), rs.getLong("cnt"), rs.getDouble("total")),
                params.toArray());
    }

    public List<TrendPointDTO> getTrend(PaymentFilter filter) {

        List<Object> params = new ArrayList<>();
        String where = buildWhereClause(filter, params);

        String sql = "select date(created_at) as day, count(*) as cnt, coalesce(sum(amount),0) as total "
                + "from payments " + where
                + " group by date(created_at) order by day asc";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new TrendPointDTO(String.valueOf(rs.getDate("day")), rs.getLong("cnt"), rs.getDouble("total")),
                params.toArray());
    }

    public List<Payment> getFilteredPayments(PaymentFilter filter, int limit) {

        List<Object> params = new ArrayList<>();
        String where = buildWhereClause(filter, params);
        int safeLimit = Math.max(1, Math.min(limit, 500));

        String sql = "select * from payments " + where + " order by created_at desc limit " + safeLimit;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Payment.class), params.toArray());
    }

    public java.util.Map<String, Object> getOverview(PaymentFilter filter) {

        List<Object> params = new ArrayList<>();
        String where = buildWhereClause(filter, params);

        String sql = "select count(*) as total, coalesce(sum(amount),0) as total_amount, "
                + "sum(case when upper(coalesce(status,'')) = 'COMPLETED' then 1 else 0 end) as completed_count, "
                + "sum(case when upper(coalesce(status,'')) = 'FAILED' then 1 else 0 end) as failed_count "
                + "from payments " + where;

        return jdbcTemplate.queryForMap(sql, params.toArray());
    }
}
