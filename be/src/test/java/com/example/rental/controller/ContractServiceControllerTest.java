package com.example.rental.controller;

import com.example.rental.dto.contractservice.ContractServiceRequest;
import com.example.rental.dto.contractservice.ContractServiceResponse;
import com.example.rental.service.ContractServiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@DisplayName("ContractServiceController tests")
class ContractServiceControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private ContractServiceService contractServiceService;

    @Test
    @WithMockUser(roles = "TENANT")
    void addService_shouldReturn201() throws Exception {
        ContractServiceRequest req = new ContractServiceRequest();
        when(contractServiceService.addServiceToContract(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(ContractServiceResponse.builder().id(1L).build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/contracts/7/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(201));
    }
}
