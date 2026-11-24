package com.choutuan.gateway.health;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 适配 K3s 存活/就绪探针的 WebFlux 响应式健康检查端点。
 */
@RestController
@RequestMapping("/gateway/api/v1")
public class GatewayHealthController {

    private static final String SERVICE_NAME = "gateway-service";
    private static final String COMMISSION_POLICY = "ZERO_COMMISSION";

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("service", SERVICE_NAME);
        payload.put("commissionPolicy", COMMISSION_POLICY);
        payload.put("timestamp", Instant.now().toString());
        return Mono.just(ResponseEntity.ok(payload));
    }
}
