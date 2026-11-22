package com.choutuan.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiTenantGatewayFilterTest {

    private final MultiTenantGatewayFilter filter = new MultiTenantGatewayFilter();

    @Test
    void getOrder_returnsHighestPrecedence() {
        assertEquals(Ordered.HIGHEST_PRECEDENCE, filter.getOrder());
    }

    @Test
    void filter_validMultiTenantHeaders_propagatesZeroCommissionPolicyAndGeneratesTraceId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header("X-App-Id", "com.choutuan.app")
                .header("X-Tenant-Id", "T1001")
                .header("X-Client-Role", "CONSUMER")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            forwardedExchange.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        ServerWebExchange resultExchange = forwardedExchange.get();
        assertNotNull(resultExchange);
        assertEquals("T1001", resultExchange.getRequest().getHeaders().getFirst("X-Tenant-Id"));
        assertEquals("CONSUMER", resultExchange.getRequest().getHeaders().getFirst("X-Client-Role"));
        assertEquals("ZERO_COMMISSION", resultExchange.getRequest().getHeaders().getFirst("X-Commission-Policy"));

        String traceId = resultExchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        assertNotNull(traceId);
        assertFalse(traceId.isBlank());
        assertEquals(traceId, resultExchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
        assertEquals("ZERO_COMMISSION", resultExchange.getResponse().getHeaders().getFirst("X-Commission-Policy"));
    }

    @Test
    void filter_existingTraceId_preservesTraceIdAndCommissionPolicy() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header("X-App-Id", "com.choutuan.app")
                .header("X-Tenant-Id", "T2002")
                .header("X-Client-Role", "MERCHANT")
                .header("X-Trace-Id", "trace-fixed-001")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            forwardedExchange.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        ServerWebExchange resultExchange = forwardedExchange.get();
        assertNotNull(resultExchange);
        assertEquals("trace-fixed-001", resultExchange.getRequest().getHeaders().getFirst("X-Trace-Id"));
        assertEquals("trace-fixed-001", resultExchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
        assertEquals("ZERO_COMMISSION", resultExchange.getRequest().getHeaders().getFirst("X-Commission-Policy"));
    }

    @Test
    void filter_invalidOrMissingTenantHeaders_returnsBadRequestAndStopsChain() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header("X-App-Id", "com.invalid.app")
                .header("X-Tenant-Id", "T1001")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainInvoked.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertFalse(chainInvoked.get());
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertTrue(exchange.getResponse().isCommitted());
    }
}
