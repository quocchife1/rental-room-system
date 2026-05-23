package com.example.rental.controller;

import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.service.DamageReportService;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
@DisplayName("DamageReportController – Integration Tests")
class DamageReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DamageReportService damageReportService;

    private DamageReportResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new DamageReportResponse();
        sampleResponse.setId(1L);
        sampleResponse.setContractId(10L);
        sampleResponse.setStatus("DRAFT");
        sampleResponse.setDescription("Phong bi hu hai");
        sampleResponse.setTotalDamageCost(new BigDecimal("500000"));
    }

    // =========================================================
    // 1. POST /api/damage-reports (multipart)
    // =========================================================
    @Nested
    @DisplayName("POST /api/damage-reports")
    class CreateDamageReportTests {

        @Test
        @WithMockUser(roles = "MAINTENANCE")
        @DisplayName("✅ Create damage report (json) → 201")
        void createDamageReport_withJson_shouldReturn201() throws Exception {
            DamageReportCreateRequest req = new DamageReportCreateRequest();
            req.setContractId(10L);
            req.setDescription("Mo ta hu hai");
            req.setTotalDamageCost(new BigDecimal("500000"));

            MockMultipartFile damageReportPart = new MockMultipartFile(
                    "damageReport",
                    "damageReport.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(req)
            );
            MockMultipartFile image = new MockMultipartFile(
                    "images",
                    "photo.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "dummy".getBytes()
            );

            when(damageReportService.createDamageReport(any(DamageReportCreateRequest.class), any()))
                    .thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/damage-reports")
                            .file(damageReportPart)
                            .file(image)
                            .param("contractId", "10"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Create damage report (plain text) → 201 + description")
        void createDamageReport_withPlainText_shouldMapDescription() throws Exception {
            MockMultipartFile damageReportPart = new MockMultipartFile(
                    "damageReport",
                    "damageReport.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Mo ta don gian".getBytes()
            );

            when(damageReportService.createDamageReport(any(DamageReportCreateRequest.class), any()))
                    .thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/damage-reports")
                            .file(damageReportPart))
                    .andDo(print())
                    .andExpect(status().isCreated());

            ArgumentCaptor<DamageReportCreateRequest> captor = ArgumentCaptor.forClass(DamageReportCreateRequest.class);
            verify(damageReportService, times(1)).createDamageReport(captor.capture(), any());
            Assertions.assertThat(captor.getValue().getDescription()).isEqualTo("Mo ta don gian");
        }

        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT create damage report → 403")
        void createDamageReport_asTenant_shouldReturn403() throws Exception {
            MockMultipartFile damageReportPart = new MockMultipartFile(
                    "damageReport",
                    "damageReport.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    "{}".getBytes()
            );

            mockMvc.perform(multipart("/api/damage-reports")
                            .file(damageReportPart))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(damageReportService);
        }
    }

    // =========================================================
    // 2. GET /api/damage-reports/{id}
    // =========================================================
    @Nested
    @DisplayName("GET /api/damage-reports/{id}")
    class GetByIdTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Get by id → 200")
        void getById_shouldReturn200() throws Exception {
            when(damageReportService.getById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/damage-reports/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT get by id → 403")
        void getById_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/damage-reports/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 3. GET /api/damage-reports/contract/{contractId}
    // =========================================================
    @Nested
    @DisplayName("GET /api/damage-reports/contract/{contractId}")
    class GetByContractTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Get by contract → 200 + list")
        void getByContract_shouldReturn200() throws Exception {
            when(damageReportService.getByContractId(10L))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/damage-reports/contract/10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)));
        }
    }

    // =========================================================
    // 4. GET /api/damage-reports/status/{status}
    // =========================================================
    @Nested
    @DisplayName("GET /api/damage-reports/status/{status}")
    class GetByStatusTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Get by status → 200 + list")
        void getByStatus_shouldReturn200() throws Exception {
            when(damageReportService.getByStatus("DRAFT"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/damage-reports/status/DRAFT"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // =========================================================
    // 5. GET /api/damage-reports
    // =========================================================
    @Nested
    @DisplayName("GET /api/damage-reports")
    class GetAllTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Get all → 200 + list")
        void getAll_shouldReturn200() throws Exception {
            when(damageReportService.getAll()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/damage-reports"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // =========================================================
    // 6. PUT /api/damage-reports/{id} (multipart)
    // =========================================================
    @Nested
    @DisplayName("PUT /api/damage-reports/{id}")
    class UpdateDamageReportTests {

        @Test
        @WithMockUser(roles = "MAINTENANCE")
        @DisplayName("✅ Update damage report → 200")
        void updateDamageReport_shouldReturn200() throws Exception {
            MockMultipartFile damageReportPart = new MockMultipartFile(
                    "damageReport",
                    "damageReport.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    "{\"description\":\"Cap nhat\"}".getBytes()
            );

            when(damageReportService.updateDamageReport(eq(1L), any(DamageReportCreateRequest.class), any()))
                    .thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/damage-reports/1")
                            .file(damageReportPart)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Cập nhật báo cáo hư hỏng thành công"));
        }
    }

    // =========================================================
    // 7. POST /api/damage-reports/{id}/submit
    // =========================================================
    @Nested
    @DisplayName("POST /api/damage-reports/{id}/submit")
    class SubmitTests {

        @Test
        @WithMockUser(roles = "MAINTENANCE")
        @DisplayName("✅ Submit report → 200")
        void submit_shouldReturn200() throws Exception {
            when(damageReportService.submitForApproval(1L)).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/damage-reports/1/submit"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Gửi báo cáo để duyệt thành công"));
        }
    }

    // =========================================================
    // 8. POST /api/damage-reports/{id}/approve
    // =========================================================
    @Nested
    @DisplayName("POST /api/damage-reports/{id}/approve")
    class ApproveTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Approve report → 200")
        void approve_shouldReturn200() throws Exception {
            when(damageReportService.approveDamageReport(eq(1L), eq("OK")))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post("/api/damage-reports/1/approve")
                            .param("approverNote", "OK"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Phê duyệt báo cáo hư hỏng thành công"));
        }

        @Test
        @WithMockUser(roles = "MAINTENANCE")
        @DisplayName("❌ MAINTENANCE approve → 403")
        void approve_asMaintenance_shouldReturn403() throws Exception {
            mockMvc.perform(post("/api/damage-reports/1/approve"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 9. POST /api/damage-reports/{id}/reject
    // =========================================================
    @Nested
    @DisplayName("POST /api/damage-reports/{id}/reject")
    class RejectTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Reject report → 200")
        void reject_shouldReturn200() throws Exception {
            when(damageReportService.rejectDamageReport(eq(1L), eq("Reject")))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post("/api/damage-reports/1/reject")
                            .param("rejectReason", "Reject"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Từ chối báo cáo hư hỏng thành công"));
        }
    }

    // =========================================================
    // 10. DELETE /api/damage-reports/{id}
    // =========================================================
    @Nested
    @DisplayName("DELETE /api/damage-reports/{id}")
    class DeleteTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Delete report → 200")
        void delete_shouldReturn200() throws Exception {
            doNothing().when(damageReportService).deleteDamageReport(1L);

            mockMvc.perform(delete("/api/damage-reports/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Xóa báo cáo hư hỏng thành công"));
        }
    }
}
