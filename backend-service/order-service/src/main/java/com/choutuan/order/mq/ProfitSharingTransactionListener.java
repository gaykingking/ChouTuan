package com.choutuan.order.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQTransactionListener
@RequiredArgsConstructor
public class ProfitSharingTransactionListener implements RocketMQLocalTransactionListener {

    private static final BigDecimal FIXED_CHANNEL_FEE = new BigDecimal("0.20");

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String orderId = getHeader(msg, "orderId");
        Long userId = parseLong(getHeader(msg, "userId"));
        Long merchantId = parseLong(getHeader(msg, "merchantId"));
        Long riderId = parseLong(getHeader(msg, "riderId"));
        BigDecimal goodsAmount = parseDecimal(getHeader(msg, "goodsAmount"));
        BigDecimal freightAmount = parseDecimal(getHeader(msg, "freightAmount"));

        if (orderId == null || userId == null || goodsAmount == null || freightAmount == null) {
            log.warn("Invalid profit-sharing message headers, orderId={}", orderId);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        try {
            Boolean committed = transactionTemplate.execute(status -> {
                Integer exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(1) FROM t_profit_sharing_ledger WHERE order_id = ? AND user_id = ?",
                        Integer.class, orderId, userId);
                if (exists != null && exists > 0) {
                    log.info("Profit-sharing ledger already persisted idempotently, orderId={}", orderId);
                    return true;
                }

                // 商家 100% 货款 + 骑手 100% 运费 + 固定 ¥0.20 通道费
                jdbcTemplate.update(
                        "INSERT INTO t_profit_sharing_ledger " +
                                "(order_id, user_id, merchant_id, rider_id, merchant_amount, rider_amount, channel_fee, status) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        orderId, userId, merchantId, riderId,
                        goodsAmount, freightAmount, FIXED_CHANNEL_FEE, "SETTLED"
                );
                return true;
            });
            return Boolean.TRUE.equals(committed)
                    ? RocketMQLocalTransactionState.COMMIT
                    : RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception ex) {
            log.error("Failed to execute profit-sharing local transaction, orderId={}", orderId, ex);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String orderId = getHeader(msg, "orderId");
        Long userId = parseLong(getHeader(msg, "userId"));
        if (orderId == null || userId == null) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM t_profit_sharing_ledger WHERE order_id = ? AND user_id = ? AND status = 'SETTLED'",
                Integer.class, orderId, userId);
        return (exists != null && exists > 0)
                ? RocketMQLocalTransactionState.COMMIT
                : RocketMQLocalTransactionState.ROLLBACK;
    }

    private String getHeader(Message<?> msg, String key) {
        Object val = msg.getHeaders().get(key);
        if (val instanceof byte[]) {
            return new String((byte[]) val, StandardCharsets.UTF_8);
        }
        return val != null ? String.valueOf(val) : null;
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        try {
            return value == null ? null : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 15 分钟超时未支付自动关单延迟消息消费者 (DelayLevel=16 -> 900s)
     */
    @Slf4j
    @Component
    @RocketMQMessageListener(
            topic = "CHOUTUAN_ORDER_TIMEOUT_TOPIC",
            consumerGroup = "choutuan-order-timeout-consumer-group"
    )
    @RequiredArgsConstructor
    public static class OrderTimeoutCancelConsumer implements RocketMQListener<String> {

        private final JdbcTemplate jdbcTemplate;

        @Override
        public void onMessage(String orderId) {
            if (orderId == null || orderId.isBlank()) {
                return;
            }
            int updated = jdbcTemplate.update(
                    "UPDATE t_order SET order_status = 'CANCELLED_TIMEOUT', update_time = NOW() " +
                            "WHERE order_id = ? AND order_status = 'UNPAID'",
                    orderId.trim()
            );
            if (updated > 0) {
                log.info("Closed unpaid order after 15m timeout, orderId={}", orderId);
            } else {
                log.debug("Order already paid or closed, skip timeout cancel, orderId={}", orderId);
            }
        }
    }
}
