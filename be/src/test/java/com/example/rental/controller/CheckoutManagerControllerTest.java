package com.example.rental.controller;

import com.example.rental.dto.checkout.CheckoutRequestManagerRow;
import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.dto.invoice.InvoiceResponse;
import com.example.rental.service.CheckoutManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Thêm
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CheckoutManagerController – Integration Tests")
class CheckoutManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CheckoutManagerService checkoutManagerService;

    private CheckoutRequestManagerRow sampleRow;
    private DamageReportResponse sampleReport;

    @BeforeEach
    void setUp() {
        sampleRow = new CheckoutRequestManagerRow();
        sampleRow.setId(1L);
        sampleRow.setStatus("PENDING");
        sampleRow.setBranchCode("CN01");
        sampleRow.setRoomNumber("101");

        sampleReport = new DamageReportResponse();
        sampleReport.setId(10L);
        sampleReport.setStatus("DRAFT");
    }

    // =========================================================
    // 1. GET /api/checkout-requests/my-branch
    // =========================================================
    @Nested
    @DisplayName("GET /api/checkout-requests/my-branch")
    class ListMyBranchTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER list my-branch → 200 + page")
        void listMyBranch_shouldReturn200() throws Exception {
            Page<CheckoutRequestManagerRow> page = new PageImpl<>(List.of(sampleRow));
            when(checkoutManagerService.listMyBranchRequests(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/checkout-requests/my-branch")
                            .param("status", "PENDING")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", org.hamcrest.Matchers.hasSize(1)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ADMIN list my-branch → 403")
        void listMyBranch_asAdmin_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/checkout-requests/my-branch"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(checkoutManagerService);
        }
    }

    // =========================================================
    // 2. PUT /api/checkout-requests/{id}/approve
    // =========================================================
    @Nested
    @DisplayName("PUT /api/checkout-requests/{id}/approve")
    class ApproveTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Approve request → 200")
        void approve_shouldReturn200() throws Exception {
            when(checkoutManagerService.approve(1L)).thenReturn(sampleRow);

            mockMvc.perform(put("/api/checkout-requests/1/approve"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Đã duyệt yêu cầu trả phòng"));
        }
    }

    // =========================================================
    // 3. GET /api/checkout-requests/{id}/inspection-report
    // =========================================================
    @Nested
    @DisplayName("GET /api/checkout-requests/{id}/inspection-report")
    class GetOrCreateReportTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Get inspection report → 200")
        void getOrCreateReport_shouldReturn200() throws Exception {
            when(checkoutManagerService.getOrCreateInspectionReport(1L)).thenReturn(sampleReport);

            mockMvc.perform(get("/api/checkout-requests/1/inspection-report"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(10));
        }
    }

    // =========================================================
    // 4. PUT /api/checkout-requests/{id}/inspection-report
    // =========================================================
    @Nested
    @DisplayName("PUT /api/checkout-requests/{id}/inspection-report")
    class SaveReportTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Save inspection report → 200")
        void saveReport_shouldReturn200() throws Exception {
            DamageReportCreateRequest req = new DamageReportCreateRequest();
            req.setDescription("Cap nhat bien ban");

            when(checkoutManagerService.saveInspectionReport(eq(1L), any(DamageReportCreateRequest.class)))
                    .thenReturn(sampleReport);

            mockMvc.perform(put("/api/checkout-requests/1/inspection-report")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Đã lưu biên bản"));
        }
    }

    // =========================================================
    // 5. POST /api/checkout-requests/{id}/inspection-report/items/{itemKey}/images
    // =========================================================
    @Nested
    @DisplayName("POST /api/checkout-requests/{id}/inspection-report/items/{itemKey}/images")
    class UploadItemImagesTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Upload item images → 200")
        void uploadItemImages_shouldReturn200() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "images",
                    "photo.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "dummy".getBytes()
            );

            when(checkoutManagerService.uploadItemImages(eq(1L), eq("WALL"), any()))
                    .thenReturn(sampleReport);

            mockMvc.perform(multipart("/api/checkout-requests/1/inspection-report/items/WALL/images")
                            .file(image))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Đã tải ảnh"));
        }
    }

    // =========================================================
    // 6. POST /api/checkout-requests/{id}/inspection-report/create-invoice
    // =========================================================
    @Nested
    @DisplayName("POST /api/checkout-requests/{id}/inspection-report/create-invoice")
    class CreateInvoiceTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Create invoice → 201")
        void createInvoice_shouldReturn201() throws Exception {
            InvoiceResponse invoice = InvoiceResponse.builder().id(99L).status("PENDING").build();
            when(checkoutManagerService.createSettlementInvoice(eq(1L), any(LocalDate.class)))
                    .thenReturn(invoice);

            mockMvc.perform(post("/api/checkout-requests/1/inspection-report/create-invoice")
                            .param("dueDate", "2026-05-30"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201));
        }
    }
}
