package com.example.rental.controller;

import com.example.rental.entity.Partners;
import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.PartnerPayment;
import com.example.rental.entity.ServicePackage;
//import com.example.rental.entity.PaymentMethod;
import com.example.rental.repository.PartnerPaymentRepository;
import com.example.rental.repository.PartnerPostRepository;
import com.example.rental.repository.PartnerRepository;
import com.example.rental.repository.ServicePackageRepository;
import com.example.rental.service.util.CodeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.when;

@DisplayName("PartnerPaymentController tests")
class PartnerPaymentControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private PartnerPaymentRepository partnerPaymentRepository;

    @MockitoBean
    private PartnerPostRepository partnerPostRepository;

    @MockitoBean
    private PartnerRepository partnerRepository;

    @MockitoBean
    private ServicePackageRepository servicePackageRepository;

    @MockitoBean
    private CodeGenerator codeGenerator;

    @Test
    @WithMockUser(username = "partner1", roles = "PARTNER")
    void createPayment_forbidden_whenNotOwner() throws Exception {
        Partners p = Partners.builder().id(1L).username("partner1").build();
        Partners other = Partners.builder().id(2L).username("other").build();
        PartnerPost post = PartnerPost.builder().id(10L).partner(other).build();

        when(partnerRepository.findByUsername("partner1")).thenReturn(java.util.Optional.of(p));
        when(partnerPostRepository.findById(10L)).thenReturn(java.util.Optional.of(post));

        String payload = "{\"postId\":10,\"amount\":1000,\"method\":\"CASH\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/partner-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "partner1", roles = "PARTNER")
    void simulatePurchase_shouldReturn200() throws Exception {
        Partners p = Partners.builder().id(1L).username("partner1").build();
        PartnerPost post = PartnerPost.builder().id(10L).partner(p).title("T").build();
        ServicePackage pkg = ServicePackage.builder().id(2L).name("P").price(java.math.BigDecimal.valueOf(1000)).build();

        when(partnerRepository.findByUsername("partner1")).thenReturn(java.util.Optional.of(p));
        when(partnerPostRepository.findById(10L)).thenReturn(java.util.Optional.of(post));
        when(servicePackageRepository.findById(2L)).thenReturn(java.util.Optional.of(pkg));
        when(partnerPaymentRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> {
            PartnerPayment pp = (PartnerPayment) i.getArgument(0);
            pp.setId(123L);
            return pp;
        });
        when(codeGenerator.generateCode(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong())).thenReturn("PAY-123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/partner-payment/simulate-purchase").param("postId", "10").param("packageId", "2"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
    }
}
