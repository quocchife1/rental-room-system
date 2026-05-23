package com.example.rental.controller;

import com.example.rental.dto.dashboard.DirectorDashboardDTO;
import com.example.rental.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@DisplayName("DashboardController tests")
class DashboardControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser(roles = "DIRECTOR")
    void getDirectorDashboard_shouldReturn200() throws Exception {
        DirectorDashboardDTO dto = new DirectorDashboardDTO();
        when(dashboardService.getDirectorDashboard(null)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/dashboard/director"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
    }

    @Test
    @WithMockUser(roles = "DIRECTOR")
    void getDashboardByDateRange_missingParams_shouldReturn400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/dashboard/director/date-range"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
