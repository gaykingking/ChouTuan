package com.choutuan.product.health;

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
@RequestMapping("/product-service/api/v1")
public class ProductHealthController {

    private static final String SERVICE_NAME = "product-service";
    private static final String COMMISSION_POLICY = "ZERO_COMMISSION";

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("service", SERVICE_NAME);
        payload.put("commissionPolicy", COMMISSION_POLICY);
        payload.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(payload);
    }
}
