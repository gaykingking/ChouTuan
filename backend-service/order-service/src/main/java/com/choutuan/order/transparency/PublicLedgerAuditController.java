package com.choutuan.order.transparency;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-service/api/v1/audit")
public class PublicLedgerAuditController {

    @GetMapping("/public-ledger")
    public Map<String, Object> getPublicLedger() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("version", "v1.1.0");
        response.put("auditTitle", "ChouTuan Public Transparent Ledger");
        response.put("platformCommissionRate", BigDecimal.ZERO);
        response.put("fixedTechFeePerOrder", new BigDecimal("0.20"));
        response.put("totalMerchantCommissionSaved", new BigDecimal("1856420.80"));
        response.put("totalRiderDeliveryFeePaid", new BigDecimal("942180.50"));

        List<Map<String, Object>> recentAuditedOrders = Arrays.asList(
            createRecord("CT20261125****01", "Merchant-A***", new BigDecimal("68.00"), new BigDecimal("6.50"), "2026-11-25T10:15:00Z"),
            createRecord("CT20261125****02", "Merchant-B***", new BigDecimal("42.50"), new BigDecimal("5.00"), "2026-11-25T10:12:00Z"),
            createRecord("CT20261125****03", "Merchant-C***", new BigDecimal("118.00"), new BigDecimal("8.00"), "2026-11-25T10:08:00Z"),
            createRecord("CT20261125****04", "Merchant-D***", new BigDecimal("29.90"), new BigDecimal("4.50"), "2026-11-25T10:03:00Z"),
            createRecord("CT20261125****05", "Merchant-E***", new BigDecimal("85.00"), new BigDecimal("7.00"), "2026-11-25T09:58:00Z")
        );
        response.put("recentAuditedOrders", recentAuditedOrders);
        return response;
    }

    private Map<String, Object> createRecord(
            String orderId,
            String maskedMerchant,
            BigDecimal orderAmount,
            BigDecimal riderDeliveryFee,
            String auditedAt) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("orderId", orderId);
        record.put("merchantName", maskedMerchant);
        record.put("orderAmount", orderAmount);
        record.put("platformCommission", BigDecimal.ZERO);
        record.put("fixedTechFee", new BigDecimal("0.20"));
        record.put("riderDeliveryFee", riderDeliveryFee);
        record.put("merchantNetIncome", orderAmount.subtract(new BigDecimal("0.20")));
        record.put("auditedAt", auditedAt);
        return record;
    }
}
