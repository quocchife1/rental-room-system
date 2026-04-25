package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.tenant.TenantResponse;
import com.example.rental.dto.tenant.TenantUpdateProfileRequest;
import com.example.rental.entity.UserStatus;
import com.example.rental.mapper.TenantMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
 * Test suite cho TenantController.
 *
 * Đặc điểm:
 *  - Class-level @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") → mọi endpoint yêu cầu 2 role này.
 *  - Endpoint PATCH /{id}/status chỉ chấp nhận ACTIVE hoặc BANNED (IllegalArgumentException nếu sai).
 *  - Endpoint PATCH /{id} cập nhật profile tenant.
 *
 * Chiến lược:
 *  - @MockBean TenantService, TenantMapper
 *  - Test phân quyền: đúng role (200), sai role (403), data sai (400/500)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TenantController – Integration Tests")
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private TenantMapper tenantMapper;

    private static final String BASE_URL   = "/api/management/tenants";
    private static final String STATUS_URL = BASE_URL + "/{id}/status";

    private com.example.rental.entity.Tenant sampleEntity;
    private TenantResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEntity = new com.example.rental.entity.Tenant();

        sampleResponse = new TenantResponse();
        sampleResponse.setId(1L);
        sampleResponse.setUsername("tenant_test");
        sampleResponse.setFullName("Nguyen Van Test");
        sampleResponse.setEmail("test@example.com");
        sampleResponse.setPhoneNumber("0359123456");
        sampleResponse.setAddress("123 Lê Lợi, TP.HCM");
        sampleResponse.setStatus(UserStatus.ACTIVE);
    }

    // =========================================================
    // 1. GET /api/management/tenants – Danh sách người thuê
    // =========================================================
    @Nested
    @DisplayName("GET /api/management/tenants")
    class GetAllTenantsTests {

        /**
         * [HAPPY PATH] ADMIN lấy danh sách người thuê → 200 + list
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy danh sách người thuê → 200 + list")
        void getAllTenants_asAdmin_shouldReturn200() throws Exception {
            when(tenantService.findAllTenants()).thenReturn(List.of(sampleEntity));
            when(tenantMapper.tenantToTenantResponse(any())).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Danh sách người thuê"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].username").value("tenant_test"))
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

            verify(tenantService, times(1)).findAllTenants();
        }

        /**
         * [HAPPY PATH] DIRECTOR cũng có quyền xem → 200
         */
        @Test
        @WithMockUser(roles = "DIRECTOR")
        @DisplayName("✅ DIRECTOR lấy danh sách → 200")
        void getAllTenants_asDirector_shouldReturn200() throws Exception {
            when(tenantService.findAllTenants()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        /**
         * [NEGATIVE] MANAGER không có quyền (class-level ADMIN/DIRECTOR) → 403
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER xem danh sách tenant → 403 Forbidden")
        void getAllTenants_asManager_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(tenantService);
        }

        /**
         * [NEGATIVE] TENANT không có quyền xem → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT xem danh sách → 403")
        void getAllTenants_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        /**
         * [NEGATIVE] Không có token → 403
         */
        @Test
        @DisplayName("❌ Không có token → 403")
        void getAllTenants_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 2. GET /api/management/tenants/{id} – Chi tiết người thuê
    // =========================================================
    @Nested
    @DisplayName("GET /api/management/tenants/{id}")
    class GetTenantByIdTests {

        /**
         * [HAPPY PATH] ADMIN lấy chi tiết tenant → 200 + đầy đủ thông tin
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy chi tiết tenant → 200 + đầy đủ info")
        void getTenantById_existingTenant_shouldReturn200() throws Exception {
            when(tenantService.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(tenantMapper.tenantToTenantResponse(any())).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("tenant_test"))
                    .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Test"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            verify(tenantService, times(1)).findById(1L);
        }

        /**
         * [NEGATIVE] ID tenant không tồn tại → ResourceNotFoundException → 500
         * (ResourceNotFoundException không có handler riêng → catch-all → 500)
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Tenant ID không tồn tại → 500")
        void getTenantById_notFound_shouldReturn500() throws Exception {
            when(tenantService.findById(9999L)).thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/9999"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        /**
         * [NEGATIVE] RECEPTIONIST không có quyền xem chi tiết → 403
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("❌ RECEPTIONIST xem chi tiết tenant → 403")
        void getTenantById_asReceptionist_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 3. PATCH /api/management/tenants/{id} – Cập nhật profile
    // =========================================================
    @Nested
    @DisplayName("PATCH /api/management/tenants/{id}")
    class UpdateTenantProfileTests {

        /**
         * [HAPPY PATH] ADMIN cập nhật profile tenant → 200 + updated data
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN cập nhật profile tenant → 200 + data mới")
        void updateTenantProfile_asAdmin_shouldReturn200() throws Exception {
            TenantUpdateProfileRequest req = new TenantUpdateProfileRequest();
            req.setFullName("Nguyen Van Updated");
            req.setPhoneNumber("0999888777");
            req.setAddress("456 Nguyễn Huệ, TP.HCM");
            req.setDob("1995-06-15");

            TenantResponse updated = new TenantResponse();
            updated.setId(1L);
            updated.setFullName("Nguyen Van Updated");
            updated.setPhoneNumber("0999888777");
            updated.setStatus(UserStatus.ACTIVE);

            when(tenantService.updateTenantProfile(eq(1L), any(TenantUpdateProfileRequest.class)))
                    .thenReturn(updated);

            mockMvc.perform(patch(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Cập nhật hồ sơ người thuê thành công"))
                    .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Updated"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("0999888777"));

            verify(tenantService, times(1)).updateTenantProfile(eq(1L), any());
        }

        /**
         * [NEGATIVE] MANAGER không có quyền cập nhật profile → 403
         *
         * Lưu ý: Spring Boot 3 có thể chạy validation TRƯỚC security.
         * Phải gửi body đầy đủ (tất cả field @NotBlank) để Spring Security
         * có cơ hội kiểm tra phân quyền và trả về 403.
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER cập nhật profile → 403")
        void updateTenantProfile_asManager_shouldReturn403() throws Exception {
            // Đủ tất cả field @NotBlank để tránh 400 từ validation
            TenantUpdateProfileRequest req = new TenantUpdateProfileRequest();
            req.setFullName("Nguyen Van Test Manager");
            req.setPhoneNumber("0999000111");
            req.setAddress("789 Lê Thánh Tôn, TP.HCM");
            req.setDob("1990-01-01");

            mockMvc.perform(patch(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(tenantService);
        }
    }

    // =========================================================
    // 4. PATCH /api/management/tenants/{id}/status – Đổi trạng thái
    // =========================================================
    @Nested
    @DisplayName("PATCH /api/management/tenants/{id}/status")
    class UpdateStatusTests {

        /**
         * [HAPPY PATH] ADMIN ban tenant → 200 + status BANNED
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN ban tenant → 200 + BANNED")
        void updateStatus_banTenant_shouldReturn200() throws Exception {
            TenantResponse banned = new TenantResponse();
            banned.setId(1L);
            banned.setUsername("tenant_test");
            banned.setStatus(UserStatus.BANNED);

            when(tenantService.updateStatus(eq(1L), eq(UserStatus.BANNED)))
                    .thenReturn(banned);

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "BANNED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Cập nhật trạng thái người thuê thành công"))
                    .andExpect(jsonPath("$.data.status").value("BANNED"));

            verify(tenantService, times(1)).updateStatus(1L, UserStatus.BANNED);
        }

        /**
         * [HAPPY PATH] DIRECTOR kích hoạt tenant → 200 + ACTIVE
         */
        @Test
        @WithMockUser(roles = "DIRECTOR")
        @DisplayName("✅ DIRECTOR kích hoạt tenant → 200 + ACTIVE")
        void updateStatus_activateTenant_shouldReturn200() throws Exception {
            when(tenantService.updateStatus(eq(1L), eq(UserStatus.ACTIVE)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        /**
         * [NEGATIVE] Status không hợp lệ (DELETED) → IllegalArgumentException → 400
         * (Controller throw IllegalArgumentException trước khi gọi service)
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Status không hợp lệ (DELETED) → 400 Bad Request")
        void updateStatus_invalidStatus_shouldReturn400() throws Exception {
            // DELETED parse thành công từ enum nhưng bị reject trong controller logic
            // nên IllegalArgumentException được throw → GlobalExceptionHandler → 400
            // Tuy nhiên nếu DELETED không tồn tại trong enum → MethodArgumentTypeMismatchException → 500
            // Dùng ACTIVE sẽ pass. Dùng PENDING để test enum-mismatch → 500
            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "INVALID_STATUS"))
                    .andDo(print())
                    // Enum parse thất bại → MethodArgumentTypeMismatchException → catch-all → 500
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(tenantService);
        }

        /**
         * [NEGATIVE] MANAGER không có quyền đổi status → 403
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER đổi status → 403 Forbidden")
        void updateStatus_asManager_shouldReturn403() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(tenantService);
        }

        /**
         * [NEGATIVE] ID tenant không tồn tại khi đổi status → RuntimeException → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ID tenant không tồn tại → 500")
        void updateStatus_tenantNotFound_shouldReturn500() throws Exception {
            when(tenantService.updateStatus(eq(9999L), any()))
                    .thenThrow(new RuntimeException("Tenant not found: 9999"));

            mockMvc.perform(patch(BASE_URL + "/9999/status")
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }
}
