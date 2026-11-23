package com.choutuan.order.health;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 适配 K3s 存活/就绪探针的健康检查端点。
 */
@RestController
@RequestMapping("/order-service/api/v1")
public class HealthCheckController {

    private static final String SERVICE_NAME = "order-service";
    private static final String COMMISSION_POLICY = "ZERO_COMMISSION";
    private static final BigDecimal FIXED_CHANNEL_FEE = new BigDecimal("0.20");

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("service", SERVICE_NAME);
        payload.put("commissionPolicy", COMMISSION_POLICY);
        payload.put("fixedChannelFee", FIXED_CHANNEL_FEE);
        payload.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(payload);
    }
}
