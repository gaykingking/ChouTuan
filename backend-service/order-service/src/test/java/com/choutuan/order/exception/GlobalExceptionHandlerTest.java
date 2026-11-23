package com.choutuan.order.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleIllegalArgument_returnsBadRequestWithStandardFields() {
        MDC.put("traceId", "trace-illegal-arg-001");
        IllegalArgumentException ex = new IllegalArgumentException("Invalid order quantity");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("code"));
        assertEquals("Invalid order quantity", body.get("message"));
        assertEquals("trace-illegal-arg-001", body.get("traceId"));
        assertNotNull(body.get("timestamp"));
        assertDoesNotThrow(() -> Instant.parse(body.get("timestamp").toString()));
    }

    @Test
    void handleIllegalArgument_withBlankMessage_usesDefaultMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("   ");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("code"));
        assertEquals("Invalid request argument", body.get("message"));
        assertNotNull(body.get("traceId"));
        assertFalse(body.get("traceId").toString().isBlank());
        assertNotNull(body.get("timestamp"));
        assertDoesNotThrow(() -> Instant.parse(body.get("timestamp").toString()));
    }

    @Test
    void handleMissingRequestHeader_returnsBadRequestWithStandardFields() throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyHeaderMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-User-Id", parameter);

        ResponseEntity<Map<String, Object>> response = handler.handleMissingRequestHeader(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("code"));
        assertEquals("Missing required request header: X-User-Id", body.get("message"));
        assertNotNull(body.get("traceId"));
        assertFalse(body.get("traceId").toString().isBlank());
        assertNotNull(body.get("timestamp"));
        assertDoesNotThrow(() -> Instant.parse(body.get("timestamp").toString()));
    }

    @Test
    void handleGenericException_returnsInternalServerErrorWithStandardFields() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("code"));
        assertEquals("Internal server error", body.get("message"));
        assertNotNull(body.get("traceId"));
        assertFalse(body.get("traceId").toString().isBlank());
        assertNotNull(body.get("timestamp"));
        assertDoesNotThrow(() -> Instant.parse(body.get("timestamp").toString()));
    }

    @SuppressWarnings("unused")
    private void dummyHeaderMethod(String userId) {
    }
}
