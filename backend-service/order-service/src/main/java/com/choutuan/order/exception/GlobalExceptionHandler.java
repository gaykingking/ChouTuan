package com.choutuan.order.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<Map<String, Object>> handleMissingRequestHeader(
      MissingRequestHeaderException ex) {
    String message = "Missing required request header: " + ex.getHeaderName();
    log.warn("Missing request header: {}", ex.getHeaderName());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(
      IllegalArgumentException ex) {
    String message =
        (ex.getMessage() != null && !ex.getMessage().isBlank())
            ? ex.getMessage()
            : "Invalid request argument";
    log.warn("Illegal argument: {}", message);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    String traceId = resolveTraceId();
    log.error("Unhandled exception [traceId={}]", traceId, ex);
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", traceId);
  }

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      HttpStatus status, String message) {
    return buildErrorResponse(status, message, resolveTraceId());
  }

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      HttpStatus status, String message, String traceId) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", status.value());
    body.put("message", message);
    body.put("traceId", traceId);
    body.put("timestamp", Instant.now().toString());
    return ResponseEntity.status(status).body(body);
  }

  private String resolveTraceId() {
    String traceId = MDC.get("traceId");
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString().replace("-", "");
    }
    return traceId;
  }
}
