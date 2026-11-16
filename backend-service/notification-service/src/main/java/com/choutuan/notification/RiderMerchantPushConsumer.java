package com.choutuan.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * RocketMQ 多租户商家与骑手实时语音播报及 PushPlus 微信通知分发器
 */
@Component
@RocketMQMessageListener(
        topic = "choutuan-order-events",
        consumerGroup = "notification-service-push-group"
)
public class RiderMerchantPushConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(RiderMerchantPushConsumer.class);
    private static final String PUSHPLUS_API_URL = "https://www.pushplus.plus/send";

    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.pushplus.default-token:}")
    private String defaultPushPlusToken;

    public RiderMerchantPushConsumer(
            SimpMessagingTemplate messagingTemplate,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.path("eventType").asText("");
            String tenantId = event.path("tenantId").asText("default");
            String orderNo = event.path("orderNo").asText("");
            String merchantId = event.path("merchantId").asText("");
            String riderId = event.path("riderId").asText("");
            String pushToken = event.path("pushPlusToken").asText(defaultPushPlusToken);

            switch (eventType) {
                case "ORDER_CREATED":
                    handleOrderCreated(tenantId, merchantId, orderNo, event, pushToken);
                    break;
                case "MERCHANT_ACCEPTED_ZERO_COMMISSION":
                    handleMerchantAccepted(tenantId, merchantId, riderId, orderNo, event, pushToken);
                    break;
                case "GROUP_BUY_VERIFIED":
                    handleGroupBuyVerified(tenantId, merchantId, orderNo, event, pushToken);
                    break;
                case "RIDER_DELIVERED_FULL_FEE":
                    handleRiderDelivered(tenantId, merchantId, riderId, orderNo, event, pushToken);
                    break;
                default:
                    log.warn("未识别的通知事件类型: tenantId={}, eventType={}", tenantId, eventType);
            }
        } catch (Exception ex) {
            log.error("处理 RocketMQ 推送消息失败: payload={}", message, ex);
        }
    }

    private void handleOrderCreated(
            String tenantId, String merchantId, String orderNo, JsonNode event, String pushToken) {
        String amount = event.path("amount").asText("0.00");
        String voiceText = String.format("您有新的零抽成订单 %s，实付金额 %s 元，请及时接单", orderNo, amount);
        sendWebSocketVoice(tenantId, "merchant", merchantId, "ORDER_CREATED", voiceText, orderNo);
        sendPushPlusReceipt(pushToken, "新订单提醒（0抽成）",
                String.format("租户：%s\n单号：%s\n金额：¥%s\n状态：待商家接单", tenantId, orderNo, amount));
    }

    private void handleMerchantAccepted(
            String tenantId, String merchantId, String riderId, String orderNo, JsonNode event, String pushToken) {
        String deliveryFee = event.path("deliveryFee").asText("0.00");
        String merchantVoice = String.format("订单 %s 已零抽成接单，正在呼叫骑手", orderNo);
        sendWebSocketVoice(tenantId, "merchant", merchantId, "MERCHANT_ACCEPTED", merchantVoice, orderNo);

        if (!riderId.isEmpty()) {
            String riderVoice = String.format("您有新的全额运费配送单 %s，运费 %s 元全额归您", orderNo, deliveryFee);
            sendWebSocketVoice(tenantId, "rider", riderId, "RIDER_ASSIGNED", riderVoice, orderNo);
        }
        sendPushPlusReceipt(pushToken, "商家0抽成接单回执",
                String.format("租户：%s\n单号：%s\n平台抽成：¥0.00\n骑手全额运费：¥%s", tenantId, orderNo, deliveryFee));
    }

    private void handleGroupBuyVerified(
            String tenantId, String merchantId, String orderNo, JsonNode event, String pushToken) {
        String voucherCode = event.path("voucherCode").asText("");
        String amount = event.path("amount").asText("0.00");
        String voiceText = String.format("团购券 %s 核销成功，到账 %s 元", voucherCode, amount);
        sendWebSocketVoice(tenantId, "merchant", merchantId, "GROUP_BUY_VERIFIED", voiceText, orderNo);
        sendPushPlusReceipt(pushToken, "团购核销入账回执",
                String.format("租户：%s\n单号：%s\n核销码：%s\n实收金额：¥%s", tenantId, orderNo, voucherCode, amount));
    }

    private void handleRiderDelivered(
            String tenantId, String merchantId, String riderId, String orderNo, JsonNode event, String pushToken) {
        String deliveryFee = event.path("deliveryFee").asText("0.00");
        if (!riderId.isEmpty()) {
            String riderVoice = String.format("订单 %s 已送达，全额运费 %s 元已结算到账", orderNo, deliveryFee);
            sendWebSocketVoice(tenantId, "rider", riderId, "RIDER_DELIVERED", riderVoice, orderNo);
        }
        String merchantVoice = String.format("订单 %s 骑手已送达", orderNo);
        sendWebSocketVoice(tenantId, "merchant", merchantId, "RIDER_DELIVERED", merchantVoice, orderNo);
        sendPushPlusReceipt(pushToken, "骑手全额运费送达账单回执",
                String.format("租户：%s\n单号：%s\n骑手：%s\n全额运费结算：¥%s", tenantId, orderNo, riderId, deliveryFee));
    }

    private void sendWebSocketVoice(
            String tenantId, String role, String targetId, String eventType, String voiceText, String orderNo) {
        if (targetId == null || targetId.isEmpty()) {
            return;
        }
        String destination = String.format("/topic/tenant/%s/%s/%s/voice", tenantId, role, targetId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("tenantId", tenantId);
        payload.put("role", role);
        payload.put("targetId", targetId);
        payload.put("eventType", eventType);
        payload.put("orderNo", orderNo);
        payload.put("voiceText", voiceText);
        payload.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend(destination, payload);
        log.info("已推送 WebSocket 语音播报: destination={}, orderNo={}", destination, orderNo);
    }

    private void sendPushPlusReceipt(String token, String title, String content) {
        if (token == null || token.isEmpty()) {
            log.debug("未配置 PushPlus Token，跳过微信账单回执推送: title={}", title);
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("title", title);
            body.put("content", content);
            body.put("template", "txt");
            restTemplate.postForEntity(PUSHPLUS_API_URL, new HttpEntity<>(body, headers), String.class);
            log.info("PushPlus 微信账单回执推送成功: title={}", title);
        } catch (Exception ex) {
            log.warn("PushPlus 微信通知发送异常: title={}", title, ex);
        }
    }
}
