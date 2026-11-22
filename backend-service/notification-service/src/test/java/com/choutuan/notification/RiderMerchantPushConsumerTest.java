package com.choutuan.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RiderMerchantPushConsumerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private RiderMerchantPushConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new RiderMerchantPushConsumer(messagingTemplate, restTemplate, objectMapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBroadcastVoiceToMerchantWhenOrderCreated() {
        String message = "{"
                + "\"eventType\":\"ORDER_CREATED\","
                + "\"tenantId\":\"tenant-001\","
                + "\"orderNo\":\"ORD20261122001\","
                + "\"merchantId\":\"M1001\","
                + "\"amount\":\"36.50\""
                + "}";

        consumer.onMessage(message);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/tenant/tenant-001/merchant/M1001/voice"), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("tenant-001", payload.get("tenantId"));
        assertEquals("merchant", payload.get("role"));
        assertEquals("M1001", payload.get("targetId"));
        assertEquals("ORDER_CREATED", payload.get("eventType"));
        assertEquals("ORD20261122001", payload.get("orderNo"));
        assertTrue(payload.get("voiceText").toString().contains("36.50"));
        assertNotNull(payload.get("timestamp"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBroadcastVoiceToMerchantAndRiderWhenMerchantAcceptedZeroCommission() {
        String message = "{"
                + "\"eventType\":\"MERCHANT_ACCEPTED_ZERO_COMMISSION\","
                + "\"tenantId\":\"tenant-002\","
                + "\"orderNo\":\"ORD20261122002\","
                + "\"merchantId\":\"M2001\","
                + "\"riderId\":\"R3001\","
                + "\"deliveryFee\":\"8.00\""
                + "}";

        consumer.onMessage(message);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate, times(2))
                .convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        List<String> destinations = destinationCaptor.getAllValues();
        List<Map<String, Object>> payloads = payloadCaptor.getAllValues();

        assertEquals("/topic/tenant/tenant-002/merchant/M2001/voice", destinations.get(0));
        assertEquals("MERCHANT_ACCEPTED", payloads.get(0).get("eventType"));
        assertEquals("merchant", payloads.get(0).get("role"));
        assertEquals("M2001", payloads.get(0).get("targetId"));

        assertEquals("/topic/tenant/tenant-002/rider/R3001/voice", destinations.get(1));
        assertEquals("RIDER_ASSIGNED", payloads.get(1).get("eventType"));
        assertEquals("rider", payloads.get(1).get("role"));
        assertEquals("R3001", payloads.get(1).get("targetId"));
        assertTrue(payloads.get(1).get("voiceText").toString().contains("8.00"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBroadcastVoiceToRiderAndMerchantWhenRiderDeliveredFullFee() {
        String message = "{"
                + "\"eventType\":\"RIDER_DELIVERED_FULL_FEE\","
                + "\"tenantId\":\"tenant-003\","
                + "\"orderNo\":\"ORD20261122003\","
                + "\"merchantId\":\"M4001\","
                + "\"riderId\":\"R5001\","
                + "\"deliveryFee\":\"12.50\""
                + "}";

        consumer.onMessage(message);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate, times(2))
                .convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        List<String> destinations = destinationCaptor.getAllValues();
        List<Map<String, Object>> payloads = payloadCaptor.getAllValues();

        assertEquals("/topic/tenant/tenant-003/rider/R5001/voice", destinations.get(0));
        assertEquals("RIDER_DELIVERED", payloads.get(0).get("eventType"));
        assertEquals("rider", payloads.get(0).get("role"));
        assertEquals("R5001", payloads.get(0).get("targetId"));
        assertTrue(payloads.get(0).get("voiceText").toString().contains("12.50"));

        assertEquals("/topic/tenant/tenant-003/merchant/M4001/voice", destinations.get(1));
        assertEquals("RIDER_DELIVERED", payloads.get(1).get("eventType"));
        assertEquals("merchant", payloads.get(1).get("role"));
        assertEquals("M4001", payloads.get(1).get("targetId"));
    }
}
