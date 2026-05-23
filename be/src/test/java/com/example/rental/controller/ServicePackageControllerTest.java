package com.example.rental.controller;

import com.example.rental.entity.ServicePackage;
import com.example.rental.repository.ServicePackageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@DisplayName("ServicePackageController tests")
class ServicePackageControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private ServicePackageRepository servicePackageRepository;

    @Test
    @WithMockUser(roles = "PARTNER")
    void getActivePackages_shouldReturn200() throws Exception {
        ServicePackage p = ServicePackage.builder().id(1L).name("Basic").price(new BigDecimal("10000")).isActive(true).build();
        when(servicePackageRepository.findByIsActiveTrueOrderByPriceAsc()).thenReturn(List.of(p));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/service-packages"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray());
    }
}
