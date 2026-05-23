package com.example.rental.controller;

import com.example.rental.dto.rentalservice.RentalServiceResponse;
import com.example.rental.dto.rentalservice.RentalServiceRequest;
import com.example.rental.service.RentalServiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@DisplayName("RentalServiceController tests")
class RentalServiceControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private RentalServiceService rentalServiceService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn201() throws Exception {
        RentalServiceRequest req = new RentalServiceRequest();
        when(rentalServiceService.create(org.mockito.ArgumentMatchers.any())).thenReturn(RentalServiceResponse.builder().id(1L).build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(201));
    }
}
