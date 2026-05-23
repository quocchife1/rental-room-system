package com.example.rental.controller;

import com.example.rental.dto.system.SystemConfigDto;
import com.example.rental.dto.system.SystemConfigUpsertRequest;
import com.example.rental.service.SystemConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@DisplayName("SystemConfigController tests")
class SystemConfigControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private SystemConfigService systemConfigService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getConfig_shouldReturn200() throws Exception {
        when(systemConfigService.get()).thenReturn(SystemConfigDto.builder().build());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/system-config"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_shouldReturn200() throws Exception {
        SystemConfigUpsertRequest req = new SystemConfigUpsertRequest();
        when(systemConfigService.upsert(org.mockito.ArgumentMatchers.any())).thenReturn(SystemConfigDto.builder().build());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/system-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
    }
}
