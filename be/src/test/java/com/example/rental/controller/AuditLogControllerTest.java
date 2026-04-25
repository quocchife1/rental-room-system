package com.example.rental.controller;

import com.example.rental.dto.audit.AuditLogDTO;
import com.example.rental.entity.AuditAction;
import com.example.rental.service.AuditLogService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite cho AuditLogController.
 *
 * Chiến lược xác thực trong test:
 *  - @WithMockUser(roles = "ADMIN") → giả lập user đã đăng nhập với role ADMIN.
 *  - Không cần JWT token thật, Spring Security Test tự inject Authentication.
 *  - @MockBean AuditLogService → không chạm DB, return fake data.
 *
 * Các nhóm test:
 *  1. GET /api/audit-logs/{targetType}/{targetId}       → getAuditTrail
 *  2. GET /api/audit-logs/paged                         → getAllPaged
 *  3. GET /api/audit-logs/{auditLogId}                  → getById
 *  4. Kiểm tra phân quyền (401/403) khi không có token
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuditLogController – Integration Tests")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    // URL constants
    private static final String AUDIT_BASE_URL   = "/api/audit-logs";
    private static final String TRAIL_URL        = AUDIT_BASE_URL + "/{type}/{id}";
    private static final String PAGED_URL        = AUDIT_BASE_URL + "/paged";
    private static final String BY_ID_URL        = AUDIT_BASE_URL + "/{auditLogId}";

    // Fake data dùng chung
    private AuditLogDTO sampleLog;
    private List<AuditLogDTO> sampleLogList;

    @BeforeEach
    void setUp() {
        // Tạo một AuditLogDTO mẫu – không lấy từ DB thật
        sampleLog = AuditLogDTO.builder()
                .id(1L)
                .actorId("admin_user")
                .actorRole("ADMIN")
                .action(AuditAction.LOGIN_SUCCESS)
                .targetType("AUTH")
                .targetId(null)
                .description("Admin đăng nhập")
                .ipAddress("127.0.0.1")
                .userAgent("MockMvc")
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
                .status("SUCCESS")
                .build();

        sampleLogList = List.of(sampleLog);
    }

    // =========================================================
    // 1. GET audit trail của một entity
    // =========================================================
    @Nested
    @DisplayName("GET /api/audit-logs/{targetType}/{targetId}")
    class GetAuditTrailTests {

        /**
         * [HAPPY PATH] ADMIN lấy audit trail → 200, data có phần tử.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy audit trail → 200 + có data")
        void getAuditTrail_asAdmin_shouldReturn200WithData() throws Exception {
            // Arrange: Service trả về danh sách log
            when(auditLogService.getAuditTrail(eq("AUTH"), eq(1L)))
                    .thenReturn(sampleLogList);

            // Act + Assert
            mockMvc.perform(get(AUDIT_BASE_URL + "/AUTH/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Audit trail fetched"))
                    // data là array, phần tử đầu tiên có id = 1
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].actorId").value("admin_user"))
                    .andExpect(jsonPath("$.data[0].status").value("SUCCESS"));

            verify(auditLogService, times(1)).getAuditTrail("AUTH", 1L);
        }

        /**
         * [HAPPY PATH] Audit trail trống → 200, data là array rỗng.
         * Đảm bảo API không trả 404 khi không có log.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Audit trail rỗng → 200 + data là []")
        void getAuditTrail_emptyResult_shouldReturn200WithEmptyArray() throws Exception {
            when(auditLogService.getAuditTrail(any(), any()))
                    .thenReturn(List.of());

            mockMvc.perform(get(AUDIT_BASE_URL + "/ROOM/999"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        /**
         * [NEGATIVE] Không có token → 403 Forbidden.
         *
         * Lý do: Spring Security mặc định trả 403 khi không có Authentication
         * (chưa cấu hình AuthenticationEntryPoint riêng để trả 401).
         * Đây là behavior đúng của project này.
         */
        @Test
        @DisplayName("❌ Không có token → 403 (Spring Security default)")
        void getAuditTrail_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(get(AUDIT_BASE_URL + "/AUTH/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(auditLogService);
        }

        /**
         * [NEGATIVE] Role không đủ quyền (ROLE_GUEST) → 403 Forbidden.
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ Role GUEST không đủ quyền → 403 Forbidden")
        void getAuditTrail_asGuest_shouldReturn403() throws Exception {
            mockMvc.perform(get(AUDIT_BASE_URL + "/AUTH/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(auditLogService);
        }
    }

    // =========================================================
    // 2. GET tất cả audit logs (phân trang)
    // =========================================================
    @Nested
    @DisplayName("GET /api/audit-logs/paged")
    class GetAllPagedTests {

        /**
         * [HAPPY PATH] ADMIN lấy danh sách phân trang → 200, có content.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Lấy audit logs phân trang → 200 + content có data")
        void getAllPaged_asAdmin_shouldReturn200WithContent() throws Exception {
            // Arrange: Page<AuditLogDTO> với 1 phần tử
            Page<AuditLogDTO> mockPage = new PageImpl<>(sampleLogList);
            when(auditLogService.getAll(any(Pageable.class))).thenReturn(mockPage);

            // Act + Assert
            mockMvc.perform(get(PAGED_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].actorRole").value("ADMIN"))
                    // Kiểm tra pagination metadata
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1));

            verify(auditLogService, times(1)).getAll(any(Pageable.class));
        }

        /**
         * [HAPPY PATH] MANAGER cũng có quyền xem → 200.
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER cũng có quyền xem paged → 200")
        void getAllPaged_asManager_shouldReturn200() throws Exception {
            Page<AuditLogDTO> mockPage = new PageImpl<>(List.of());
            when(auditLogService.getAll(any(Pageable.class))).thenReturn(mockPage);

            mockMvc.perform(get(PAGED_URL))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] Không có token → 403 Forbidden (Spring Security default).
         */
        @Test
        @DisplayName("❌ Không có token → 403 (Spring Security default)")
        void getAllPaged_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(get(PAGED_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 3. GET chi tiết audit log theo ID
    // =========================================================
    @Nested
    @DisplayName("GET /api/audit-logs/{auditLogId}")
    class GetByIdTests {

        /**
         * [HAPPY PATH] Lấy chi tiết log tồn tại → 200 + đầy đủ fields.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Lấy audit log theo ID → 200 + đầy đủ thông tin")
        void getById_existingLog_shouldReturn200WithFullDetail() throws Exception {
            when(auditLogService.getById(1L)).thenReturn(sampleLog);

            mockMvc.perform(get(AUDIT_BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.actorId").value("admin_user"))
                    .andExpect(jsonPath("$.data.actorRole").value("ADMIN"))
                    .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.ipAddress").value("127.0.0.1"));

            verify(auditLogService, times(1)).getById(1L);
        }

        /**
         * [NEGATIVE] Log không tồn tại → Service throw RuntimeException → 500.
         * (Nếu dự án thêm NotFoundException thì test này sẽ expect 404)
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Audit log không tồn tại → 500 (hoặc 404 nếu có NotFoundException)")
        void getById_notExistingLog_shouldReturnError() throws Exception {
            when(auditLogService.getById(9999L))
                    .thenThrow(new RuntimeException("Audit log không tồn tại: 9999"));

            mockMvc.perform(get(AUDIT_BASE_URL + "/9999"))
                    .andDo(print())
                    // GlobalExceptionHandler bắt Exception → 500
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.statusCode").value(500));
        }
    }

    // =========================================================
    // 4. GET audit logs theo actor
    // =========================================================
    @Nested
    @DisplayName("GET /api/audit-logs/actor/{actorId}")
    class GetByActorTests {

        /**
         * [HAPPY PATH] MANAGER lấy log của một actor → 200 + data.
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER lấy log theo actor → 200 + có data")
        void getByActor_asManager_shouldReturn200() throws Exception {
            Page<AuditLogDTO> mockPage = new PageImpl<>(sampleLogList);
            when(auditLogService.getByActorId(eq("admin_user"), any(Pageable.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(get(AUDIT_BASE_URL + "/actor/admin_user")
                            .param("page", "0")
                            .param("size", "5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].actorId").value("admin_user"));
        }

        /**
         * [NEGATIVE] TENANT không có quyền xem actor logs → 403.
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT không đủ quyền xem actor logs → 403")
        void getByActor_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get(AUDIT_BASE_URL + "/actor/some_user"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(auditLogService);
        }
    }
}
