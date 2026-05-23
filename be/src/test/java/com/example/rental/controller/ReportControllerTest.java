package com.example.rental.controller;

import com.example.rental.dto.reports.FinancialReportSummaryDTO;
import com.example.rental.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.mockito.Mockito.when;

@DisplayName("ReportController tests")
class ReportControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private ReportService reportService;

    @Test
    @WithMockUser(roles = "ACCOUNTANT")
    void summary_shouldReturn200() throws Exception {
        FinancialReportSummaryDTO dto = FinancialReportSummaryDTO.builder().build();
        when(reportService.getSummary(LocalDate.now().minusDays(1), LocalDate.now(), null)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/summary")
                        .param("from", LocalDate.now().minusDays(1).toString())
                        .param("to", LocalDate.now().toString()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
    }

    @Test
    @WithMockUser(roles = "ACCOUNTANT")
    void summary_missingParams_shouldReturn400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/reports/summary"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
