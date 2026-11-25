package com.choutuan.order.transparency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicLedgerAuditControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PublicLedgerAuditController controller = new PublicLedgerAuditController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void shouldReturnPublicLedgerWithZeroCommissionAndRecentAuditedOrders() throws Exception {
        mockMvc.perform(get("/order-service/api/v1/audit/public-ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformCommissionRate").value(0))
                .andExpect(jsonPath("$.fixedTechFeePerOrder").value(0.20))
                .andExpect(jsonPath("$.recentAuditedOrders").isArray())
                .andExpect(jsonPath("$.recentAuditedOrders").isNotEmpty())
                .andExpect(jsonPath("$.recentAuditedOrders.length()").value(5));
    }
}
