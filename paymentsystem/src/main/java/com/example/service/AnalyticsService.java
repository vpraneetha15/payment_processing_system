package com.example.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.dto.AnalyticsOverviewDTO;
import com.example.dto.CurrencyVolumeDTO;
import com.example.dto.ErrorCodeCountDTO;
import com.example.dto.PaymentFilter;
import com.example.dto.StatusCountDTO;
import com.example.dto.TrendPointDTO;
import com.example.model.Payment;
import com.example.repository.AnalyticsRepository;

@Service
public class AnalyticsService {

    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public PaymentFilter buildFilter(String status, String currency, String errorCode,
            String from, String to, Double minAmount, Double maxAmount) {

        PaymentFilter filter = new PaymentFilter();

        filter.setStatuses(splitToList(status));
        filter.setCurrencies(splitToList(currency));
        filter.setErrorCodes(splitToList(errorCode));

        if (from != null && !from.isBlank()) {
            filter.setFromDate(LocalDate.parse(from).atStartOfDay());
        }
        if (to != null && !to.isBlank()) {
            filter.setToDate(LocalDate.parse(to).atTime(LocalTime.MAX));
        }

        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);

        return filter;
    }

    private List<String> splitToList(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    public List<StatusCountDTO> getStatusDistribution(PaymentFilter filter) {
        return repository.getStatusDistribution(filter);
    }

    public List<ErrorCodeCountDTO> getErrorCodeBreakdown(PaymentFilter filter) {
        return repository.getErrorCodeBreakdown(filter);
    }

    public List<CurrencyVolumeDTO> getCurrencyVolume(PaymentFilter filter) {
        return repository.getCurrencyVolume(filter);
    }

    public List<TrendPointDTO> getTrend(PaymentFilter filter) {
        return repository.getTrend(filter);
    }

    public List<Payment> getFilteredPayments(PaymentFilter filter, int limit) {
        return repository.getFilteredPayments(filter, limit);
    }

    public AnalyticsOverviewDTO getOverview(PaymentFilter filter) {

        Map<String, Object> row = repository.getOverview(filter);

        long total = toLong(row.get("total"));
        long completed = toLong(row.get("completed_count"));
        long failed = toLong(row.get("failed_count"));

        AnalyticsOverviewDTO overview = new AnalyticsOverviewDTO();
        overview.setTotalPayments(total);
        overview.setTotalAmount(toDouble(row.get("total_amount")));
        overview.setCompletedCount(completed);
        overview.setFailedCount(failed);
        overview.setInFlightCount(Math.max(0, total - completed - failed));
        overview.setSuccessRate(total == 0 ? 0.0 : (completed * 100.0) / total);
        overview.setFailureRate(total == 0 ? 0.0 : (failed * 100.0) / total);

        return overview;
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
        return Double.parseDouble(value.toString());
    }
}
