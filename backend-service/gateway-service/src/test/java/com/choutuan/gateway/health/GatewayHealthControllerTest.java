package com.choutuan.gateway.health;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GatewayHealthControllerTest {

    @Test
    void healthShouldReturnUpStatusAndZeroCommissionPolicy() {
        ResponseEntity<Map<String, Object>> response = new GatewayHealthController().health().block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("gateway-service", body.get("service"));
        assertEquals("ZERO_COMMISSION", body.get("commissionPolicy"));
        assertNotNull(body.get("timestamp"));
    }
}
