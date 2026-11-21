package com.choutuan.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MerchantOnboardingAndWithdrawControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MerchantOnboardingAndWithdrawController()).setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter()).build();
    }

    @Test
    void shouldReturn400WhenSamePricePactNotSigned() throws Exception {
        String payload = """
                {
                  "brandCode": "BRAND-001",
                  "shopName": "ChouTuan Noodle House",
                  "category": "Catering",
                  "contactPhone": "13800138000",
                  "creditCode": "91110000MA0000001X",
                  "signedSamePricePact": false
                }
                """;

        mockMvc.perform(post("/order-service/api/v1/tenants/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldApproveOnboardingWithZeroCommissionAndFixedTechFeeWhenPactSigned() throws Exception {
        String payload = """
                {
                  "brandCode": "BRAND-001",
                  "shopName": "ChouTuan Noodle House",
                  "category": "Catering",
                  "contactPhone": "13800138000",
                  "creditCode": "91110000MA0000001X",
                  "signedSamePricePact": true
                }
                """;

        mockMvc.perform(post("/order-service/api/v1/tenants/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.commissionRate").value(0))
                .andExpect(jsonPath("$.fixedTechFee").value(0.20));
    }

    @Test
    void shouldReturnZeroWithdrawFeeOnInstantWithdraw() throws Exception {
        String payload = """
                {
                  "accountRole": "MERCHANT",
                  "targetChannel": "WECHAT_PAY",
                  "amountYuan": 500.00
                }
                """;

        mockMvc.perform(post("/order-service/api/v1/withdraw/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.withdrawFee").value(0))
                .andExpect(jsonPath("$.netArrivedAmount").value(500.00));
    }
}
