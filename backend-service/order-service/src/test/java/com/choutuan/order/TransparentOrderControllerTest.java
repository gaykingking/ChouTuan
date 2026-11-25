package com.choutuan.order;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransparentOrderControllerTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TransparentOrderController controller = new TransparentOrderController(rocketMQTemplate);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void cancelOrder_returnsZeroFeeFullRefund() throws Exception {
        mockMvc.perform(post("/order-service/api/v1/orders/CT20261125001/cancel")
                        .header("X-Tenant-Id", "tenant_food_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant_food_1"))
                .andExpect(jsonPath("$.orderId").value("CT20261125001"))
                .andExpect(jsonPath("$.status").value("CANCELLED_BY_USER"))
                .andExpect(jsonPath("$.cancelFee").value(0))
                .andExpect(jsonPath("$.refundPolicy").value("ZERO_FEE_FULL_REFUND"));
    }
}
