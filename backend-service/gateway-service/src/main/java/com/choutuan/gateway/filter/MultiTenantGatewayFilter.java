package com.choutuan.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class MultiTenantGatewayFilter implements GlobalFilter, Ordered {

    private static final String HEADER_APP_ID = "X-App-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_CLIENT_ROLE = "X-Client-Role";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String HEADER_COMMISSION_POLICY = "X-Commission-Policy";

    private static final String EXPECTED_APP_ID = "com.choutuan.app";
    private static final String ZERO_COMMISSION_POLICY = "ZERO_COMMISSION";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        String appId = headers.getFirst(HEADER_APP_ID);
        String tenantId = headers.getFirst(HEADER_TENANT_ID);
        String clientRole = headers.getFirst(HEADER_CLIENT_ROLE);

        if (!EXPECTED_APP_ID.equals(appId)
                || !StringUtils.hasText(tenantId)
                || !StringUtils.hasText(clientRole)) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        String traceId = headers.getFirst(HEADER_TRACE_ID);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        final String finalTraceId = traceId;
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(HEADER_TRACE_ID, finalTraceId)
                .header(HEADER_COMMISSION_POLICY, ZERO_COMMISSION_POLICY)
                .build();

        exchange.getResponse().getHeaders().set(HEADER_TRACE_ID, finalTraceId);
        exchange.getResponse().getHeaders().set(HEADER_COMMISSION_POLICY, ZERO_COMMISSION_POLICY);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
