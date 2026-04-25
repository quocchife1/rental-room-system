package com.example.rental.controller;

import com.example.rental.dto.maintenance.MaintenanceResponse;
import com.example.rental.dto.maintenance.MaintenanceStatusUpdateRequest;
import com.example.rental.service.MaintenanceRequestService;
import com.example.rental.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
 * Test suite cho MaintenanceController.
 *
 * Phân quyền:
 *  - POST /api/maintenance                    → TENANT only (multipart)
 *  - GET  /api/maintenance/my-requests        → TENANT
 *  - GET  /api/maintenance/board              → ADMIN, MANAGER, MAINTENANCE
 *  - PATCH /api/maintenance/{id}/status       → ADMIN, MANAGER, MAINTENANCE
 *  - PUT  /api/maintenance/{id}               → ADMIN, MANAGER, MAINTENANCE
 *  - GET  /api/maintenance/tenant/{tenantId}  → công khai (không có @PreAuthorize)
 *  - GET  /api/maintenance/status/{status}    → công khai (không có @PreAuthorize)
 *
 * Chiến lược:
 *  - @MockBean MaintenanceRequestService, TenantRepository
 *  - POST multipart test dùng MockMvcRequestBuilders.multipart()
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MaintenanceController – Integration Tests")
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaintenanceRequestService maintenanceRequestService;

    @MockBean
    private TenantRepository tenantRepository;

    private static final String BASE_URL = "/api/maintenance";

    private MaintenanceResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = MaintenanceResponse.builder()
                .id(1L)
                .requestCode("MT-2025-001")
                .tenantName("Nguyen Van Test")
                .branchCode("CN01")
                .branchName("Chi nhánh Quận 1")
                .roomNumber("101")
                .description("Điều hòa không hoạt động")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .images(List.of())
                .build();
    }

    // =========================================================
    // 1. GET /api/maintenance/board – Bảng điều khiển bảo trì
    // =========================================================
    @Nested
    @DisplayName("GET /api/maintenance/board")
    class GetBoardTests {

        /**
         * [HAPPY PATH] ADMIN xem bảng bảo trì → 200 + list
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN xem board bảo trì → 200 + list")
        void getBoard_asAdmin_shouldReturn200() throws Exception {
            when(maintenanceRequestService.getAllRequests())
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/board"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Maintenance board fetched"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].requestCode").value("MT-2025-001"))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));

            verify(maintenanceRequestService, times(1)).getAllRequests();
        }

        /**
         * [HAPPY PATH] MAINTENANCE role cũng được xem board → 200
         */
        @Test
        @WithMockUser(roles = "MAINTENANCE")
        @DisplayName("✅ MAINTENANCE role xem board → 200")
        void getBoard_asMaintenance_shouldReturn200() throws Exception {
            when(maintenanceRequestService.getAllRequests()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/board"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        /**
         * [NEGATIVE] TENANT không có quyền xem board → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT xem board → 403 Forbidden")
        void getBoard_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/board"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(maintenanceRequestService);
        }
    }

    // =========================================================
    // 2. GET /api/maintenance/my-requests – Yêu cầu của Tenant
    // =========================================================
    @Nested
    @DisplayName("GET /api/maintenance/my-requests")
    class GetMyRequestsTests {

        /**
         * [HAPPY PATH] TENANT xem yêu cầu của mình → 200 + list
         */
        @Test
        @WithMockUser(username = "tenant_test", roles = "TENANT")
        @DisplayName("✅ TENANT lấy yêu cầu bảo trì của mình → 200 + list")
        void getMyRequests_asTenant_shouldReturn200() throws Exception {
            com.example.rental.entity.Tenant fakeTenant = new com.example.rental.entity.Tenant();
            fakeTenant.setId(10L);

            when(tenantRepository.findByUsernameIgnoreCase("tenant_test"))
                    .thenReturn(Optional.of(fakeTenant));
            when(maintenanceRequestService.getRequestsByTenant(10L))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/my-requests"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].requestCode").value("MT-2025-001"));
        }

        /**
         * [HAPPY PATH] Tenant không tồn tại trong DB → 404
         */
        @Test
        @WithMockUser(username = "ghost_user", roles = "TENANT")
        @DisplayName("✅ Username không tồn tại → 404 body")
        void getMyRequests_tenantNotFound_shouldReturn404() throws Exception {
            when(tenantRepository.findByUsernameIgnoreCase("ghost_user"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/my-requests"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        /**
         * [NEGATIVE] ADMIN không có quyền xem /my-requests → 403
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ADMIN xem /my-requests → 403")
        void getMyRequests_asAdmin_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/my-requests"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 3. PATCH /api/maintenance/{id}/status – Cập nhật trạng thái
    // =========================================================
    @Nested
    @DisplayName("PATCH /api/maintenance/{id}/status")
    class UpdateStatusTests {

        /**
         * [HAPPY PATH] MANAGER cập nhật trạng thái bảo trì → 200 + PROCESSING
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER cập nhật status → 200 + PROCESSING")
        void updateStatus_asManager_shouldReturn200() throws Exception {
            MaintenanceResponse processing = MaintenanceResponse.builder()
                    .id(1L).requestCode("MT-2025-001").status("PROCESSING")
                    .images(List.of()).build();

            when(maintenanceRequestService.updateStatus(eq(1L), eq("PROCESSING")))
                    .thenReturn(processing);

            MaintenanceStatusUpdateRequest body = new MaintenanceStatusUpdateRequest();
            body.setStatus("PROCESSING");

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Maintenance status updated"))
                    .andExpect(jsonPath("$.data.status").value("PROCESSING"));

            verify(maintenanceRequestService, times(1)).updateStatus(1L, "PROCESSING");
        }

        /**
         * [HAPPY PATH] MAINTENANCE role cập nhật status → 200
         */
        @Test
        @WithMockUser(roles = "MAINTENANCE")
        @DisplayName("✅ MAINTENANCE role cập nhật status → 200")
        void updateStatus_asMaintenance_shouldReturn200() throws Exception {
            when(maintenanceRequestService.updateStatus(any(), any()))
                    .thenReturn(sampleResponse);

            MaintenanceStatusUpdateRequest body = new MaintenanceStatusUpdateRequest();
            body.setStatus("DONE");

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] TENANT không có quyền cập nhật status → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT cập nhật status → 403")
        void updateStatus_asTenant_shouldReturn403() throws Exception {
            MaintenanceStatusUpdateRequest body = new MaintenanceStatusUpdateRequest();
            body.setStatus("DONE");

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(maintenanceRequestService);
        }

        /**
         * [NEGATIVE] ID không tồn tại → RuntimeException → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ID yêu cầu bảo trì không tồn tại → 500")
        void updateStatus_notFound_shouldReturn500() throws Exception {
            when(maintenanceRequestService.updateStatus(eq(9999L), any()))
                    .thenThrow(new RuntimeException("Maintenance request not found: 9999"));

            MaintenanceStatusUpdateRequest body = new MaintenanceStatusUpdateRequest();
            body.setStatus("DONE");

            mockMvc.perform(patch(BASE_URL + "/9999/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================
    // 4. PUT /api/maintenance/{id} – Cập nhật yêu cầu
    // =========================================================
    @Nested
    @DisplayName("PUT /api/maintenance/{id}")
    class UpdateRequestTests {

        /**
         * [HAPPY PATH] ADMIN cập nhật yêu cầu bảo trì → 200
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN cập nhật yêu cầu → 200 + data mới")
        void updateRequest_asAdmin_shouldReturn200() throws Exception {
            MaintenanceResponse updated = MaintenanceResponse.builder()
                    .id(1L).requestCode("MT-2025-001")
                    .status("DONE")
                    .resolution("Đã thay linh kiện điều hòa")
                    .technicianName("Nguyen Ky Thuat")
                    .images(List.of()).build();

            when(maintenanceRequestService.updateRequest(
                    eq(1L), eq("Đã thay linh kiện"), eq("DONE"), eq("Nguyen Ky Thuat"), eq("500000")))
                    .thenReturn(updated);

            mockMvc.perform(put(BASE_URL + "/1")
                            .param("resolution", "Đã thay linh kiện")
                            .param("status", "DONE")
                            .param("technician", "Nguyen Ky Thuat")
                            .param("cost", "500000"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Maintenance request updated"))
                    .andExpect(jsonPath("$.data.status").value("DONE"))
                    .andExpect(jsonPath("$.data.resolution")
                            .value("Đã thay linh kiện điều hòa"));
        }

        /**
         * [NEGATIVE] RECEPTIONIST không có quyền cập nhật → 403
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("❌ RECEPTIONIST cập nhật yêu cầu → 403")
        void updateRequest_asReceptionist_shouldReturn403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1")
                            .param("status", "DONE"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(maintenanceRequestService);
        }
    }

    // =========================================================
    // 5. GET /api/maintenance/tenant/{tenantId} – Theo tenant ID
    // =========================================================
    @Nested
    @DisplayName("GET /api/maintenance/tenant/{tenantId}")
    class GetByTenantIdTests {

        /**
         * [HAPPY PATH] Endpoint công khai → 200 (không cần login)
         * Đây là endpoint không có @PreAuthorize nên ai cũng gọi được
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Lấy yêu cầu theo tenantId → 200")
        void getByTenantId_shouldReturn200() throws Exception {
            when(maintenanceRequestService.getRequestsByTenant(5L))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/tenant/5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].tenantName").value("Nguyen Van Test"));
        }
    }

    // =========================================================
    // 6. GET /api/maintenance/status/{status} – Theo trạng thái
    // =========================================================
    @Nested
    @DisplayName("GET /api/maintenance/status/{status}")
    class GetByStatusTests {

        /**
         * [HAPPY PATH] Lấy yêu cầu PENDING → 200 + list
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Lấy yêu cầu theo status PENDING → 200")
        void getByStatus_pending_shouldReturn200() throws Exception {
            when(maintenanceRequestService.getRequestsByStatus("PENDING"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        /**
         * [HAPPY PATH] Không có yêu cầu với status DONE → 200 + []
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Không có yêu cầu DONE → 200 + []")
        void getByStatus_done_noData_shouldReturn200() throws Exception {
            when(maintenanceRequestService.getRequestsByStatus("DONE"))
                    .thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/status/DONE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }
}
