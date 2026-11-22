package com.choutuan.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TenantProductControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TenantProductController())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void listTenants_shouldSortByLbsDistanceAscending() throws Exception {
        mockMvc.perform(get("/product-service/api/v1/tenants")
                        .param("lat", "22.5405")
                        .param("lng", "113.9345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].tenantId").value("T1001"))
                .andExpect(jsonPath("$.data[0].distanceMeters").value(0))
                .andExpect(jsonPath("$.data[0].commissionLabel").value("0%抽成·堂食同价"))
                .andExpect(jsonPath("$.data[1].tenantId").value("T1002"));

        mockMvc.perform(get("/product-service/api/v1/tenants")
                        .param("lat", "22.5432")
                        .param("lng", "113.9410"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].tenantId").value("T1002"))
                .andExpect(jsonPath("$.data[0].distanceMeters").value(0))
                .andExpect(jsonPath("$.data[1].tenantId").value("T1001"));
    }

    @Test
    void upsertProduct_shouldEnforceSamePriceAndCalculate23PercentCompetitorBreakEven() throws Exception {
        String requestBody = "{"
                + "\"tenantId\":\"T1001\","
                + "\"productId\":\"P9001\","
                + "\"productName\":\"招牌水煮牛肉\","
                + "\"channel\":\"FOOD\","
                + "\"dineInPrice\":77.00,"
                + "\"takeoutPrice\":77.00,"
                + "\"available\":true,"
                + "\"stock\":50"
                + "}";

        mockMvc.perform(post("/product-service/api/v1/products/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productId").value("P9001"))
                .andExpect(jsonPath("$.data.dineInPrice").value(77.0))
                .andExpect(jsonPath("$.data.takeoutPrice").value(77.0))
                .andExpect(jsonPath("$.data.samePriceAsDineIn").value(true))
                .andExpect(jsonPath("$.data.competitorBreakEvenPrice").value(100.0))
                .andExpect(jsonPath("$.data.competitorBreakEvenPremiumAmount").value(23.0))
                .andExpect(jsonPath("$.data.competitorBreakEvenPremiumPercent").value("29.87%"));
    }
}
