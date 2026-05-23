package com.example.rental.controller;

import com.example.rental.dto.payment.PaymentRequest;
import com.example.rental.dto.payment.PaymentResponse;
import com.example.rental.entity.Payment;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.InvoiceService;
import com.example.rental.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PaymentController – Integration Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private InvoiceService invoiceService;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private com.example.rental.repository.PaymentRepository paymentRepository;

    private PaymentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new PaymentRequest();
        validRequest.setInvoiceId(1L);
        validRequest.setMethod("BANK_TRANSFER");
        validRequest.setAmount(new BigDecimal("3500000"));
        validRequest.setProviderRef("PROV-001");
    }

    // =========================================================
    // 1. POST /api/payments/pay
    // =========================================================
    @Nested
    @DisplayName("POST /api/payments/pay")
    class PayTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Process payment → 200 + data")
        void pay_shouldReturn200() throws Exception {
            PaymentResponse resp = new PaymentResponse(10L, 1L, "SUCCESS", new BigDecimal("3500000"), "BANK_TRANSFER", "PROV-001", java.time.LocalDateTime.now());
            when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(resp);

            mockMvc.perform(post("/api/payments/pay")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.paymentId").value(10))
                    .andExpect(jsonPath("$.data.status").value("SUCCESS"));

            verify(paymentService, times(1)).processPayment(any(PaymentRequest.class));
        }

        @Test
        @DisplayName("❌ Không có token → 403")
        void pay_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(post("/api/payments/pay")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(paymentService);
        }
    }

    // =========================================================
    // 2. POST /api/payments/momo/initiate
    // =========================================================
    @Nested
    @DisplayName("POST /api/payments/momo/initiate")
    class InitiateMoMoTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Initiate MoMo → 200 + redirectUrl")
        void initiateMoMo_shouldReturn200() throws Exception {
            when(paymentService.initiateMoMo(any(PaymentRequest.class), eq("/return")))
                    .thenReturn("https://momo.test/redirect");

            mockMvc.perform(post("/api/payments/momo/initiate")
                            .param("returnUrl", "/return")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.redirectUrl").value("https://momo.test/redirect"));
        }
    }

    // =========================================================
    // 3. GET /api/payments/momo/callback
    // =========================================================
    @Nested
    @DisplayName("GET /api/payments/momo/callback")
    class MoMoCallbackTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Callback SUCCESS → 200 + mark paid + audit log")
        void momoCallback_success_shouldMarkPaid() throws Exception {
            Payment payment = Payment.builder()
                    .id(1L)
                    .invoiceId(1L)
                    .providerRef("PROV-001")
                    .status("PENDING")
                    .build();

            when(paymentRepository.findAll()).thenReturn(List.of(payment));

            mockMvc.perform(get("/api/payments/momo/callback")
                            .param("invoiceId", "1")
                            .param("providerRef", "PROV-001")
                            .param("status", "SUCCESS"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Payment confirmed"));

            verify(invoiceService, times(1)).markPaid(1L, true);
            verify(auditLogService, times(1)).logAction(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository, times(1)).save(paymentCaptor.capture());
            Assertions.assertThat(paymentCaptor.getValue().getStatus()).isEqualTo("SUCCESS");
        }

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("❌ Callback FAILED → 200 + statusCode=400")
        void momoCallback_failed_shouldReturn400Body() throws Exception {
            mockMvc.perform(get("/api/payments/momo/callback")
                            .param("invoiceId", "1")
                            .param("providerRef", "PROV-001")
                            .param("status", "FAILED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value("Payment failed"));

            verifyNoInteractions(invoiceService);
        }
    }

    // =========================================================
    // 4. GET /api/payments/momo/fake-redirect
    // =========================================================
    @Nested
    @DisplayName("GET /api/payments/momo/fake-redirect")
    class MoMoFakeRedirectTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Fake redirect → 302 + Location")
        void momoFakeRedirect_shouldReturn302() throws Exception {
            mockMvc.perform(get("/api/payments/momo/fake-redirect")
                            .param("invoiceId", "1")
                            .param("providerRef", "PROV-001")
                            .param("returnUrl", "/receipt?invoiceId=1"))
                    .andDo(print())
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", "/receipt?invoiceId=1"))
                    .andExpect(jsonPath("$.statusCode").value(302))
                    .andExpect(jsonPath("$.data.location").value("/receipt?invoiceId=1"));

            verify(invoiceService, times(1)).markPaid(1L, true);
        }
    }

    // =========================================================
    // 5. GET /api/payments/paged
    // =========================================================
    @Nested
    @DisplayName("GET /api/payments/paged")
    class GetPaymentsPagedTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Get payments paged → 200 + page")
        void getPaymentsPaged_shouldReturn200() throws Exception {
            Payment payment = Payment.builder().id(1L).invoiceId(1L).status("SUCCESS").build();
            Page<Payment> page = new PageImpl<>(List.of(payment));
            when(paymentRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/payments/paged")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", org.hamcrest.Matchers.hasSize(1)));
        }
    }
}
