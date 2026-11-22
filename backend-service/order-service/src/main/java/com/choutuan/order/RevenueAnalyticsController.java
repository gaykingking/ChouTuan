package com.choutuan.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-service/api/v1/analytics")
public class RevenueAnalyticsController {

    private static final BigDecimal TRADITIONAL_COMMISSION_RATE = new BigDecimal("0.23");
    private static final BigDecimal TRADITIONAL_NET_RATIO = new BigDecimal("0.77");

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSevenDayRevenueSummary(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "tenant_food_1") String tenantId) {

        String resolvedTenant = (tenantId == null || tenantId.isBlank()) ? "tenant_food_1" : tenantId.trim();
        BigDecimal tenantScale = resolveTenantScale(resolvedTenant);

        List<BigDecimal> baseDailyGross = List.of(
                new BigDecimal("1820.00"),
                new BigDecimal("2150.00"),
                new BigDecimal("1960.00"),
                new BigDecimal("2480.00"),
                new BigDecimal("2790.00"),
                new BigDecimal("3120.00"),
                new BigDecimal("2940.00")
        );
        List<BigDecimal> baseDailyRiderFees = List.of(
                new BigDecimal("214.00"),
                new BigDecimal("248.00"),
                new BigDecimal("226.00"),
                new BigDecimal("285.00"),
                new BigDecimal("312.00"),
                new BigDecimal("356.00"),
                new BigDecimal("334.00")
        );

        BigDecimal chouTuanNetTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal traditionalNetTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal savedCommissionTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal riderFullDeliveryFeeTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<DailyRevenueItem> dailyBreakdown = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < baseDailyGross.size(); i++) {
            LocalDate date = today.minusDays(6L - i);
            BigDecimal dayGross = baseDailyGross.get(i).multiply(tenantScale).setScale(2, RoundingMode.HALF_UP);
            BigDecimal dayRiderFee = baseDailyRiderFees.get(i).multiply(tenantScale).setScale(2, RoundingMode.HALF_UP);
            BigDecimal dayTraditionalNet = dayGross.multiply(TRADITIONAL_NET_RATIO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal daySaved = dayGross.subtract(dayTraditionalNet).setScale(2, RoundingMode.HALF_UP);

            chouTuanNetTotal = chouTuanNetTotal.add(dayGross);
            traditionalNetTotal = traditionalNetTotal.add(dayTraditionalNet);
            savedCommissionTotal = savedCommissionTotal.add(daySaved);
            riderFullDeliveryFeeTotal = riderFullDeliveryFeeTotal.add(dayRiderFee);

            dailyBreakdown.add(new DailyRevenueItem(
                    date.toString(),
                    dayGross,
                    dayTraditionalNet,
                    daySaved,
                    dayRiderFee
            ));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tenantId", resolvedTenant);
        response.put("periodDays", 7);
        response.put("platformCommissionRate", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        response.put("platformCommissionFee", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        response.put("traditionalCommissionRate", TRADITIONAL_COMMISSION_RATE);
        response.put("chouTuanNetRevenue", chouTuanNetTotal);
        response.put("traditionalPlatformNetRevenue", traditionalNetTotal);
        response.put("savedCommissionTotal", savedCommissionTotal);
        response.put("riderFullDeliveryFeeTotal", riderFullDeliveryFeeTotal);
        response.put("dailyBreakdown", dailyBreakdown);

        return ResponseEntity.ok(response);
    }

    private BigDecimal resolveTenantScale(String tenantId) {
        if ("tenant_food_1".equals(tenantId)) {
            return BigDecimal.ONE;
        }
        int offset = Math.abs(tenantId.hashCode() % 5);
        return BigDecimal.valueOf(0.8 + offset * 0.15).setScale(2, RoundingMode.HALF_UP);
    }

    public record DailyRevenueItem(
            String date,
            BigDecimal chouTuanNetRevenue,
            BigDecimal traditionalPlatformNetRevenue,
            BigDecimal savedCommission,
            BigDecimal riderFullDeliveryFee
    ) {
    }
}
