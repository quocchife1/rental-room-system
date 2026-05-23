package com.example.rental.controller;

import com.example.rental.dto.booking.ServiceBookingResponse;
import com.example.rental.service.ServiceBookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;

@DisplayName("ServiceBookingController tests")
class ServiceBookingControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private ServiceBookingService serviceBookingService;

    @Test
    @WithMockUser(roles = "TENANT")
    void createCleaning_shouldReturn201() throws Exception {
        ServiceBookingResponse resp = ServiceBookingResponse.builder().id(1L).build();
        when(serviceBookingService.createNextCleaningBooking(5L, null)).thenReturn(resp);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/contracts/5/bookings/cleaning"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(201));
    }

    @Test
    @WithMockUser(roles = "TENANT")
    void listBookings_shouldReturn200() throws Exception {
        when(serviceBookingService.listBookingsForContract(5L)).thenReturn(List.of(ServiceBookingResponse.builder().id(1L).build()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/contracts/5/bookings"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray());
    }
}
