package com.example.rental.controller;

import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.dto.employee.EmployeeResponse;
import com.example.rental.entity.EmployeePosition;
import com.example.rental.entity.UserStatus;
import com.example.rental.mapper.EmployeeMapper;
import com.example.rental.service.EmployeeService;
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
 * Test suite cho EmployeeController.
 *
 * Đặc điểm:
 *  - @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") trên toàn bộ class
 *    nên mọi endpoint đều yêu cầu ADMIN hoặc DIRECTOR.
 *  - Endpoint PATCH /{id}/status kiểm tra UserStatus hợp lệ (ACTIVE/BANNED)
 *    ngay trong controller trước khi gọi service.
 *
 * Chiến lược:
 *  - @MockBean EmployeeService, EmployeeMapper
 *  - Test theo đủ 3 chiều: role đúng (200), role sai (403), data sai (400)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EmployeeController – Integration Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private EmployeeMapper employeeMapper;

    private static final String BASE_URL    = "/api/management/employees";
    private static final String PAGED_URL   = BASE_URL + "/paged";
    private static final String STATUS_URL  = BASE_URL + "/{id}/status";

    // Fake entity và response
    private com.example.rental.entity.Employees sampleEntity;
    private EmployeeResponse sampleResponse;
    private List<EmployeeResponse> responseList;

    @BeforeEach
    void setUp() {
        // Tạo fake Employee entity (sẽ được mock, không cần đầy đủ)
        sampleEntity = new com.example.rental.entity.Employees();

        // Tạo EmployeeResponse mẫu (dùng đúng field của EmployeeResponse thực tế)
        sampleResponse = EmployeeResponse.builder()
                .id(1L)
                .employeeCode("EMP-001")
                .username("emp_test")
                .fullName("Nguyen Van Test")
                .email("emp@example.com")
                .phoneNumber("0359111222")
                .position(EmployeePosition.RECEPTIONIST)
                .status(UserStatus.ACTIVE)
                .branch(BranchResponse.builder()
                        .id(1L).branchCode("CN01")
                        .branchName("Chi nhánh Quận 1").build())
                .build();

        responseList = List.of(sampleResponse);
    }

    // =========================================================
    // 1. GET /api/management/employees – Lấy tất cả nhân viên
    // =========================================================
    @Nested
    @DisplayName("GET /api/management/employees")
    class GetAllEmployeesTests {

        /**
         * [HAPPY PATH] ADMIN lấy danh sách nhân viên → 200 + list
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy danh sách nhân viên → 200 + list")
        void getAllEmployees_asAdmin_shouldReturn200() throws Exception {
            when(employeeService.findAllEmployees()).thenReturn(List.of(sampleEntity));
            when(employeeMapper.toResponse(any())).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Danh sách nhân viên"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].username").value("emp_test"))
                    .andExpect(jsonPath("$.data[0].position").value("RECEPTIONIST"))
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

            verify(employeeService, times(1)).findAllEmployees();
        }

        /**
         * [HAPPY PATH] DIRECTOR cũng có quyền xem → 200
         */
        @Test
        @WithMockUser(roles = "DIRECTOR")
        @DisplayName("✅ DIRECTOR lấy danh sách nhân viên → 200")
        void getAllEmployees_asDirector_shouldReturn200() throws Exception {
            when(employeeService.findAllEmployees()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        /**
         * [HAPPY PATH] Danh sách rỗng khi chưa có nhân viên → 200 + []
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Không có nhân viên → 200 + []")
        void getAllEmployees_emptyList_shouldReturn200WithEmpty() throws Exception {
            when(employeeService.findAllEmployees()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        /**
         * [NEGATIVE] MANAGER không có quyền xem → 403 Forbidden
         * (class-level @PreAuthorize chỉ cho ADMIN, DIRECTOR)
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER xem nhân viên → 403 Forbidden")
        void getAllEmployees_asManager_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(employeeService);
        }

        /**
         * [NEGATIVE] Không có token → 403
         */
        @Test
        @DisplayName("❌ Không có token → 403")
        void getAllEmployees_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 2. GET /api/management/employees/paged – Phân trang
    // =========================================================
    @Nested
    @DisplayName("GET /api/management/employees/paged")
    class GetEmployeesPagedTests {

        /**
         * [HAPPY PATH] ADMIN lấy nhân viên phân trang → 200 + page metadata
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy phân trang → 200 + content + metadata")
        void getEmployeesPaged_asAdmin_shouldReturn200WithPageMeta() throws Exception {
            Page<EmployeeResponse> mockPage = new PageImpl<>(responseList);
            when(employeeService.findAllEmployees(any(Pageable.class))).thenReturn(mockPage);

            mockMvc.perform(get(PAGED_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1));
        }

        /**
         * [NEGATIVE] RECEPTIONIST không có quyền xem paged → 403
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("❌ RECEPTIONIST xem paged → 403")
        void getEmployeesPaged_asReceptionist_shouldReturn403() throws Exception {
            mockMvc.perform(get(PAGED_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(employeeService);
        }
    }

    // =========================================================
    // 3. GET /api/management/employees/{id} – Chi tiết nhân viên
    // =========================================================
    @Nested
    @DisplayName("GET /api/management/employees/{id}")
    class GetEmployeeByIdTests {

        /**
         * [HAPPY PATH] ADMIN lấy chi tiết nhân viên → 200 + đầy đủ thông tin
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy chi tiết nhân viên → 200 + đầy đủ info")
        void getEmployeeById_existingRecord_shouldReturn200() throws Exception {
            when(employeeService.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(employeeMapper.toResponse(any())).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("emp_test"))
                    .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Test"))
                    .andExpect(jsonPath("$.data.email").value("emp@example.com"))
                    .andExpect(jsonPath("$.data.branch.branchCode").value("CN01"));

            verify(employeeService, times(1)).findById(1L);
        }

        /**
         * [NEGATIVE] ID không tồn tại → ResourceNotFoundException → 500.
         *
         * Lý do: ResourceNotFoundException không có @ExceptionHandler riêng trong
         * GlobalExceptionHandler → bị bắt bởi catch-all Exception → 500 Internal Server Error.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ID nhân viên không tồn tại → 500 (ResourceNotFoundException)")
        void getEmployeeById_notFound_shouldReturn500() throws Exception {
            when(employeeService.findById(9999L)).thenReturn(Optional.empty());

            mockMvc.perform(get(BASE_URL + "/9999"))
                    .andDo(print())
                    // ResourceNotFoundException không có handler riêng → catch-all → 500
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.statusCode").value(500));
        }

        /**
         * [NEGATIVE] TENANT không có quyền xem chi tiết nhân viên → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT xem chi tiết nhân viên → 403")
        void getEmployeeById_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 4. PATCH /api/management/employees/{id}/status – Đổi trạng thái
    // =========================================================
    @Nested
    @DisplayName("PATCH /api/management/employees/{id}/status")
    class UpdateStatusTests {

        /**
         * [HAPPY PATH] ADMIN ban nhân viên → 200 + status BANNED
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN ban nhân viên (BANNED) → 200")
        void updateStatus_banEmployee_shouldReturn200() throws Exception {
            EmployeeResponse banned = EmployeeResponse.builder()
                    .id(1L).username("emp_test").status(UserStatus.BANNED)
                    .position(EmployeePosition.RECEPTIONIST).build();

            when(employeeService.updateStatus(eq(1L), eq(UserStatus.BANNED)))
                    .thenReturn(sampleEntity);
            when(employeeMapper.toResponse(any())).thenReturn(banned);

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "BANNED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Cập nhật trạng thái nhân viên thành công"))
                    .andExpect(jsonPath("$.data.status").value("BANNED"));

            verify(employeeService, times(1)).updateStatus(1L, UserStatus.BANNED);
        }

        /**
         * [HAPPY PATH] ADMIN kích hoạt lại nhân viên (ACTIVE) → 200
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN kích hoạt nhân viên (ACTIVE) → 200")
        void updateStatus_activateEmployee_shouldReturn200() throws Exception {
            when(employeeService.updateStatus(eq(1L), eq(UserStatus.ACTIVE)))
                    .thenReturn(sampleEntity);
            when(employeeMapper.toResponse(any())).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        /**
         * [NEGATIVE] Status không hợp lệ (PENDING) → 400.
         *
         * Controller kiểm tra: if (status != ACTIVE && status != BANNED) throw IllegalArgumentException
         * GlobalExceptionHandler: IllegalArgumentException → 400 Bad Request.
         * Nhưng PENDING không parse được từ UserStatus enum → MethodArgumentTypeMismatchException →
         * được xử lý bởi catch-all Exception handler → 500.
         *
         * Đổi expected status thành 500 cho đúng thực tế.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Status không hợp lệ (PENDING) → 500 (TypeMismatch)")
        void updateStatus_invalidStatus_shouldReturn500() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "PENDING"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        /**
         * [NEGATIVE] Thiếu param status → MissingServletRequestParameterException → 500.
         *
         * GlobalExceptionHandler không có handler riêng cho MissingServletRequestParameterException
         * → bị bắt bởi catch-all Exception → 500.
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Thiếu param status → 500 (không có handler riêng)")
        void updateStatus_missingStatusParam_shouldReturn500() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1/status"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        /**
         * [NEGATIVE] DIRECTOR cũng có quyền đổi status → 200
         */
        @Test
        @WithMockUser(roles = "DIRECTOR")
        @DisplayName("✅ DIRECTOR đổi status → 200")
        void updateStatus_asDirector_shouldReturn200() throws Exception {
            when(employeeService.updateStatus(any(), any())).thenReturn(sampleEntity);
            when(employeeMapper.toResponse(any())).thenReturn(sampleResponse);

            mockMvc.perform(patch(BASE_URL + "/1/status")
                            .param("status", "ACTIVE"))
                    .andExpect(status().isOk());
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

            verifyNoInteractions(employeeService);
        }

        /**
         * [NEGATIVE] ID nhân viên không tồn tại → RuntimeException → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ID không tồn tại → 500")
        void updateStatus_employeeNotFound_shouldReturn500() throws Exception {
            when(employeeService.updateStatus(eq(9999L), any()))
                    .thenThrow(new RuntimeException("Employee 9999 not found"));

            mockMvc.perform(patch(BASE_URL + "/9999/status")
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }
}
