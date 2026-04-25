package com.example.rental.controller;

import com.example.rental.dto.invoice.InvoiceRequest;
import com.example.rental.dto.invoice.InvoiceResponse;
import com.example.rental.service.InvoiceService;
import com.example.rental.service.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite cho InvoiceController.
 *
 * Phân quyền:
 *  - POST /api/invoices            → ADMIN, DIRECTOR
 *  - GET  /api/invoices            → ADMIN, DIRECTOR, MANAGER, ACCOUNTANT
 *  - GET  /api/invoices/{id}       → staff + TENANT
 *  - GET  /api/invoices/paged      → ADMIN, DIRECTOR, MANAGER, ACCOUNTANT
 *  - POST /api/invoices/{id}/pay   → ADMIN, DIRECTOR, ACCOUNTANT, TENANT
 *  - GET  /api/invoices/my-invoices → TENANT only
 *  - POST /api/invoices/{id}/send-reminder → ADMIN, DIRECTOR, MANAGER, ACCOUNTANT
 *
 * Chiến lược:
 *  - @MockBean InvoiceService, TenantService để tách khỏi DB
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("InvoiceController – Integration Tests")
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private TenantService tenantService;

    private static final String BASE_URL = "/api/invoices";

    private InvoiceResponse sampleInvoice;
    private InvoiceRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleInvoice = InvoiceResponse.builder()
                .id(1L)
                .contractId(10L)
                .tenantId(5L)
                .tenantUsername("tenant_test")
                .tenantFullName("Nguyen Van Test")
                .tenantEmail("test@example.com")
                .tenantPhoneNumber("0359123456")
                .branchCode("CN01")
                .branchName("Chi nhánh Quận 1")
                .roomCode("CN01101")
                .roomNumber("101")
                .amount(new BigDecimal("3500000"))
                .dueDate(LocalDate.now().plusDays(7))
                .billingYear(2025)
                .billingMonth(4)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .details(List.of())
                .build();

        validRequest = new InvoiceRequest();
        validRequest.setContractId(10L);
    }

    // =========================================================
    // 1. GET /api/invoices – Lấy tất cả hóa đơn
    // =========================================================
    @Nested
    @DisplayName("GET /api/invoices")
    class GetAllInvoicesTests {

        /**
         * [HAPPY PATH] ADMIN lấy danh sách hóa đơn → 200 + list
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy tất cả hóa đơn → 200 + list")
        void getAllInvoices_asAdmin_shouldReturn200() throws Exception {
            when(invoiceService.getAll()).thenReturn(List.of(sampleInvoice));

            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Invoices fetched"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].tenantUsername").value("tenant_test"))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.data[0].branchCode").value("CN01"));

            verify(invoiceService, times(1)).getAll();
        }

        /**
         * [HAPPY PATH] ACCOUNTANT cũng được xem danh sách → 200
         */
        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ ACCOUNTANT xem danh sách → 200")
        void getAllInvoices_asAccountant_shouldReturn200() throws Exception {
            when(invoiceService.getAll()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] TENANT không có quyền xem tất cả hóa đơn → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT xem tất cả hóa đơn → 403")
        void getAllInvoices_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(invoiceService);
        }

        /**
         * [NEGATIVE] Không có token → 403
         */
        @Test
        @DisplayName("❌ Không có token → 403")
        void getAllInvoices_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 2. GET /api/invoices/{id} – Chi tiết hóa đơn
    // =========================================================
    @Nested
    @DisplayName("GET /api/invoices/{id}")
    class GetInvoiceByIdTests {

        /**
         * [HAPPY PATH] TENANT xem chi tiết hóa đơn của mình → 200
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("✅ TENANT xem chi tiết hóa đơn → 200 + đầy đủ fields")
        void getInvoiceById_asTenant_shouldReturn200() throws Exception {
            when(invoiceService.getById(1L)).thenReturn(sampleInvoice);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.amount").value(3500000))
                    .andExpect(jsonPath("$.data.billingYear").value(2025))
                    .andExpect(jsonPath("$.data.billingMonth").value(4));
        }

        /**
         * [HAPPY PATH] MANAGER cũng có quyền xem → 200
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER xem chi tiết hóa đơn → 200")
        void getInvoiceById_asManager_shouldReturn200() throws Exception {
            when(invoiceService.getById(1L)).thenReturn(sampleInvoice);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] Hóa đơn không tồn tại → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Hóa đơn ID không tồn tại → 500")
        void getInvoiceById_notFound_shouldReturn500() throws Exception {
            when(invoiceService.getById(9999L))
                    .thenThrow(new RuntimeException("Invoice not found: 9999"));

            mockMvc.perform(get(BASE_URL + "/9999"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        /**
         * [NEGATIVE] GUEST không có quyền xem hóa đơn → 403
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ GUEST xem hóa đơn → 403")
        void getInvoiceById_asGuest_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 3. POST /api/invoices – Tạo hóa đơn (ADMIN/DIRECTOR)
    // =========================================================
    @Nested
    @DisplayName("POST /api/invoices")
    class CreateInvoiceTests {

        /**
         * [HAPPY PATH] ADMIN tạo hóa đơn hợp lệ → 201 Created
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN tạo hóa đơn → 201 + data")
        void createInvoice_asAdmin_shouldReturn201() throws Exception {
            when(invoiceService.create(any(InvoiceRequest.class))).thenReturn(sampleInvoice);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Invoice created"))
                    .andExpect(jsonPath("$.data.contractId").value(10))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));

            verify(invoiceService, times(1)).create(any(InvoiceRequest.class));
        }

        /**
         * [NEGATIVE] ACCOUNTANT không có quyền tạo hóa đơn → 403
         */
        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("❌ ACCOUNTANT tạo hóa đơn → 403 Forbidden")
        void createInvoice_asAccountant_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(invoiceService);
        }

        /**
         * [NEGATIVE] TENANT không có quyền tạo hóa đơn → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT tạo hóa đơn → 403")
        void createInvoice_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 4. GET /api/invoices/paged – Phân trang hóa đơn
    // =========================================================
    @Nested
    @DisplayName("GET /api/invoices/paged")
    class GetPagedInvoicesTests {

        /**
         * [HAPPY PATH] ACCOUNTANT lấy hóa đơn phân trang → 200 + page
         */
        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ ACCOUNTANT lấy phân trang → 200 + page metadata")
        void getPagedInvoices_asAccountant_shouldReturn200() throws Exception {
            Page<InvoiceResponse> mockPage = new PageImpl<>(List.of(sampleInvoice));
            when(invoiceService.search(any(Pageable.class), any(), any(), any()))
                    .thenReturn(mockPage);

            mockMvc.perform(get(BASE_URL + "/paged")
                            .param("page", "0").param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        /**
         * [HAPPY PATH] Lọc theo năm và tháng → 200
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Lọc theo year=2025, month=4 → 200")
        void getPagedInvoices_withFilter_shouldReturn200() throws Exception {
            Page<InvoiceResponse> mockPage = new PageImpl<>(List.of(sampleInvoice));
            when(invoiceService.search(any(), eq(2025), eq(4), any()))
                    .thenReturn(mockPage);

            mockMvc.perform(get(BASE_URL + "/paged")
                            .param("year", "2025")
                            .param("month", "4"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].billingYear").value(2025));
        }

        /**
         * [NEGATIVE] RECEPTIONIST không có quyền xem paged → 403
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("❌ RECEPTIONIST xem paged → 403")
        void getPagedInvoices_asReceptionist_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paged"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(invoiceService);
        }
    }

    // =========================================================
    // 5. POST /api/invoices/{id}/pay – Thanh toán hóa đơn
    // =========================================================
    @Nested
    @DisplayName("POST /api/invoices/{id}/pay")
    class PayInvoiceTests {

        /**
         * [HAPPY PATH] ACCOUNTANT thanh toán trực tiếp (direct=true) → 200 + PAID
         */
        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ ACCOUNTANT thanh toán trực tiếp → 200 + PAID")
        void payInvoice_directPayment_shouldReturn200() throws Exception {
            InvoiceResponse paid = InvoiceResponse.builder()
                    .id(1L).contractId(10L).status("PAID")
                    .amount(new BigDecimal("3500000")).details(List.of()).build();

            when(invoiceService.markPaid(eq(1L), eq(true))).thenReturn(paid);

            mockMvc.perform(post(BASE_URL + "/1/pay")
                            .param("direct", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Invoice paid"))
                    .andExpect(jsonPath("$.data.status").value("PAID"));

            verify(invoiceService, times(1)).markPaid(1L, true);
        }

        /**
         * [HAPPY PATH] TENANT thanh toán hóa đơn của mình → 200
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("✅ TENANT thanh toán → 200")
        void payInvoice_asTenant_shouldReturn200() throws Exception {
            InvoiceResponse paid = InvoiceResponse.builder()
                    .id(1L).status("PAID").details(List.of()).build();

            when(invoiceService.markPaid(eq(1L), eq(true))).thenReturn(paid);

            mockMvc.perform(post(BASE_URL + "/1/pay")
                            .param("direct", "true"))
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] MANAGER không có quyền thanh toán hóa đơn → 403
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER thanh toán → 403")
        void payInvoice_asManager_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/pay")
                            .param("direct", "true"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(invoiceService);
        }
    }

    // =========================================================
    // 6. GET /api/invoices/my-invoices – Hóa đơn của Tenant
    // =========================================================
    @Nested
    @DisplayName("GET /api/invoices/my-invoices")
    class GetMyInvoicesTests {

        /**
         * [HAPPY PATH] TENANT lấy hóa đơn của mình → 200 + list
         */
        @Test
        @WithMockUser(username = "tenant_test", roles = "TENANT")
        @DisplayName("✅ TENANT lấy hóa đơn của mình → 200 + list")
        void getMyInvoices_asTenant_shouldReturn200() throws Exception {
            // Tạo fake Tenant entity
            com.example.rental.entity.Tenant fakeTenant = new com.example.rental.entity.Tenant();
            fakeTenant.setId(5L);

            when(tenantService.findByUsername("tenant_test"))
                    .thenReturn(Optional.of(fakeTenant));
            when(invoiceService.getInvoicesForTenant(5L))
                    .thenReturn(List.of(sampleInvoice));

            mockMvc.perform(get(BASE_URL + "/my-invoices"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        /**
         * [HAPPY PATH] Tenant chưa có dữ liệu → 404 (tenant not found)
         */
        @Test
        @WithMockUser(username = "unknown_user", roles = "TENANT")
        @DisplayName("✅ Tenant không tồn tại → 404 trong response body")
        void getMyInvoices_tenantNotFound_shouldReturn404Body() throws Exception {
            when(tenantService.findByUsername("unknown_user"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/my-invoices"))
                    .andDo(print())
                    // Controller trả về 404 response entity
                    .andExpect(status().isNotFound());
        }

        /**
         * [NEGATIVE] ADMIN không có quyền xem /my-invoices → 403
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ADMIN xem /my-invoices → 403")
        void getMyInvoices_asAdmin_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/my-invoices"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(invoiceService);
        }
    }

    // =========================================================
    // 7. POST /api/invoices/{id}/send-reminder – Gửi nhắc nhở
    // =========================================================
    @Nested
    @DisplayName("POST /api/invoices/{id}/send-reminder")
    class SendReminderTests {

        /**
         * [HAPPY PATH] ACCOUNTANT gửi nhắc thanh toán → 200
         */
        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ ACCOUNTANT gửi nhắc thanh toán → 200 Reminder sent")
        void sendReminder_asAccountant_shouldReturn200() throws Exception {
            doNothing().when(invoiceService).sendReminderForInvoice(1L);

            mockMvc.perform(post(BASE_URL + "/1/send-reminder"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Reminder sent"));

            verify(invoiceService, times(1)).sendReminderForInvoice(1L);
        }

        /**
         * [NEGATIVE] TENANT không có quyền gửi nhắc → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT gửi nhắc → 403 Forbidden")
        void sendReminder_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/send-reminder"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(invoiceService);
        }
    }
}
