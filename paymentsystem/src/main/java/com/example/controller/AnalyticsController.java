package com.example.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.AnalyticsOverviewDTO;
import com.example.dto.CurrencyVolumeDTO;
import com.example.dto.ErrorCodeCountDTO;
import com.example.dto.PaymentFilter;
import com.example.dto.StatusCountDTO;
import com.example.dto.TrendPointDTO;
import com.example.model.Payment;
import com.example.service.AnalyticsService;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public AnalyticsOverviewDTO getOverview(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        PaymentFilter filter = service.buildFilter(status, currency, errorCode, from, to, minAmount, maxAmount);
        return service.getOverview(filter);
    }

    @GetMapping("/status-distribution")
    public List<StatusCountDTO> getStatusDistribution(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        PaymentFilter filter = service.buildFilter(status, currency, errorCode, from, to, minAmount, maxAmount);
        return service.getStatusDistribution(filter);
    }

    @GetMapping("/error-codes")
    public List<ErrorCodeCountDTO> getErrorCodeBreakdown(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        PaymentFilter filter = service.buildFilter(status, currency, errorCode, from, to, minAmount, maxAmount);
        return service.getErrorCodeBreakdown(filter);
    }

    @GetMapping("/currency-volume")
    public List<CurrencyVolumeDTO> getCurrencyVolume(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        PaymentFilter filter = service.buildFilter(status, currency, errorCode, from, to, minAmount, maxAmount);
        return service.getCurrencyVolume(filter);
    }

    @GetMapping("/trend")
    public List<TrendPointDTO> getTrend(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        PaymentFilter filter = service.buildFilter(status, currency, errorCode, from, to, minAmount, maxAmount);
        return service.getTrend(filter);
    }

    @GetMapping("/payments")
    public List<Payment> getFilteredPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "200") int limit) {

        PaymentFilter filter = service.buildFilter(status, currency, errorCode, from, to, minAmount, maxAmount);
        return service.getFilteredPayments(filter, limit);
    }
}
