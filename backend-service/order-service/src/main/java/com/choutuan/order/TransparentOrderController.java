package com.choutuan.order;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/order-service/api/v1")
public class TransparentOrderController {

    private static final BigDecimal PLATFORM_COMMISSION_RATE = BigDecimal.ZERO;
    private static final BigDecimal FIXED_CHANNEL_FEE = new BigDecimal("0.20");
    private static final int ORDER_CLOSE_DELAY_MINUTES = 15;

    private final RocketMQTemplate rocketMQTemplate;

    public TransparentOrderController(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @PostMapping("/orders/create")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody Map<String, Object> request) {
        String orderId = "CT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal totalAmount = new BigDecimal(String.valueOf(request.getOrDefault("totalAmount", "0.00")));
        BigDecimal platformCommission = totalAmount.multiply(PLATFORM_COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal merchantNetAmount = totalAmount.subtract(FIXED_CHANNEL_FEE).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        Message<Map<String, Object>> timeoutMsg = MessageBuilder.withPayload(Map.<String, Object>of(
                "tenantId", tenantId,
                "orderId", orderId,
                "action", "AUTO_CLOSE_UNPAID",
                "delayMinutes", ORDER_CLOSE_DELAY_MINUTES
        )).build();
        rocketMQTemplate.syncSendDelayTimeSeconds("choutuan-order-timeout-topic", timeoutMsg, ORDER_CLOSE_DELAY_MINUTES * 60L);

        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "orderId", orderId,
                "status", "CREATED",
                "totalAmount", totalAmount,
                "platformCommission", platformCommission,
                "channelFee", FIXED_CHANNEL_FEE,
                "merchantNetAmount", merchantNetAmount,
                "createdAt", Instant.now().toString()
        ));
    }

    @PostMapping("/orders/{orderId}/transition")
    public ResponseEntity<Map<String, Object>> transitionOrder(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String orderId,
            @RequestBody Map<String, Object> request) {
        String targetStatus = String.valueOf(request.getOrDefault("targetStatus", "ACCEPTED"));
        BigDecimal totalAmount = new BigDecimal(String.valueOf(request.getOrDefault("totalAmount", "0.00")));
        BigDecimal merchantNetAmount = totalAmount.subtract(FIXED_CHANNEL_FEE).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        if ("COMPLETED".equalsIgnoreCase(targetStatus) || "DELIVERED".equalsIgnoreCase(targetStatus)) {
            Message<Map<String, Object>> settleMsg = MessageBuilder.withPayload(Map.<String, Object>of(
                    "tenantId", tenantId,
                    "orderId", orderId,
                    "platformCommission", PLATFORM_COMMISSION_RATE,
                    "channelFee", FIXED_CHANNEL_FEE,
                    "merchantNetAmount", merchantNetAmount
            )).build();
            rocketMQTemplate.sendMessageInTransaction("choutuan-order-settlement-topic", settleMsg, tenantId);
        }

        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "orderId", orderId,
                "status", targetStatus,
                "platformCommission", PLATFORM_COMMISSION_RATE,
                "channelFee", FIXED_CHANNEL_FEE,
                "merchantNetAmount", merchantNetAmount,
                "updatedAt", Instant.now().toString()
        ));
    }

    @PostMapping("/vouchers/{code}/redeem")
    public ResponseEntity<Map<String, Object>> redeemVoucher(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String code,
            @RequestBody(required = false) Map<String, Object> request) {
        BigDecimal voucherAmount = request != null && request.containsKey("amount")
                ? new BigDecimal(String.valueOf(request.get("amount")))
                : BigDecimal.ZERO;
        BigDecimal merchantNetAmount = voucherAmount.subtract(FIXED_CHANNEL_FEE).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        Message<Map<String, Object>> settleMsg = MessageBuilder.withPayload(Map.<String, Object>of(
                "tenantId", tenantId,
                "voucherCode", code,
                "platformCommission", PLATFORM_COMMISSION_RATE,
                "channelFee", FIXED_CHANNEL_FEE,
                "merchantNetAmount", merchantNetAmount
        )).build();
        rocketMQTemplate.sendMessageInTransaction("choutuan-voucher-settlement-topic", settleMsg, tenantId);

        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "voucherCode", code,
                "status", "REDEEMED",
                "platformCommission", PLATFORM_COMMISSION_RATE,
                "channelFee", FIXED_CHANNEL_FEE,
                "merchantNetAmount", merchantNetAmount,
                "redeemedAt", Instant.now().toString()
        ));
    }
}
