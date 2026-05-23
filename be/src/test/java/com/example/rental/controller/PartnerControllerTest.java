package com.example.rental.controller;

import com.example.rental.dto.partner.PartnerResponse;
import com.example.rental.mapper.PartnerMapper;
import com.example.rental.service.PartnerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;

//import java.util.List;

import static org.mockito.Mockito.when;

@DisplayName("PartnerController tests")
class PartnerControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private PartnerService partnerService;

    @MockitoBean
    private PartnerMapper partnerMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPartners_shouldReturn200() throws Exception {
        PartnerResponse r = PartnerResponse.builder().id(1L).companyName("A").build();
        when(partnerService.findAllPartners()).thenReturn(java.util.List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/management/partners"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPartnerById_notFound_shouldReturn404() throws Exception {
        when(partnerService.findById(99L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/management/partners/99"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
