package com.example.rental.controller;

import com.example.rental.service.MomoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@DisplayName("MomoCallbackController tests")
class MomoCallbackControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private MomoService momoService;

    @Test
    void momoReturn_shouldRedirect302() throws Exception {
        when(momoService.handleMomoReturn("ORD-1")).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/momo/return").param("orderId", "ORD-1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(302));
    }

    @Test
    void ipnHandler_shouldReturn200_evenOnException() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(momoService).handleMomoCallback(org.mockito.ArgumentMatchers.anyMap());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/momo/ipn-handler").contentType("application/json").content("{}"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
    }
}
