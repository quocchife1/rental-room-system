package com.example.rental.controller;

import com.example.rental.dto.branch.BranchRequest;
import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.service.BranchService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite cho BranchController.
 *
 * Phân quyền:
 *  - GET (tất cả) → ADMIN, DIRECTOR, MANAGER
 *  - POST / PUT / DELETE → ADMIN, DIRECTOR
 *
 * Chiến lược:
 *  - @MockBean BranchService → tách hoàn toàn khỏi DB
 *  - @WithMockUser để giả lập các role khác nhau
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("BranchController – Integration Tests")
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BranchService branchService;

    private static final String BASE_URL    = "/api/branches";
    private static final String BY_ID_URL   = BASE_URL + "/{id}";
    private static final String BY_CODE_URL = BASE_URL + "/code/{code}";

    // Fake data dùng chung
    private BranchResponse sampleBranch;
    private BranchRequest  validRequest;

    @BeforeEach
    void setUp() {
        sampleBranch = BranchResponse.builder()
                .id(1L)
                .branchCode("CN01")
                .branchName("Chi nhánh Quận 1")
                .address("123 Lê Lợi, Quận 1, TP.HCM")
                .phoneNumber("02812345678")
                .build();

        validRequest = new BranchRequest();
        validRequest.setBranchCode("CN01");
        validRequest.setBranchName("Chi nhánh Quận 1");
        validRequest.setAddress("123 Lê Lợi, Quận 1, TP.HCM");
        validRequest.setPhoneNumber("02812345678");
    }

    // =========================================================
    // 1. GET /api/branches – Lấy tất cả chi nhánh
    // =========================================================
    @Nested
    @DisplayName("GET /api/branches")
    class GetAllBranchesTests {

        /**
         * [HAPPY PATH] ADMIN lấy danh sách chi nhánh → 200 + list
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN lấy tất cả chi nhánh → 200 + có data")
        void getAllBranches_asAdmin_shouldReturn200WithList() throws Exception {
            when(branchService.getAllBranches()).thenReturn(List.of(sampleBranch));

            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Branches fetched"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].branchCode").value("CN01"))
                    .andExpect(jsonPath("$.data[0].branchName").value("Chi nhánh Quận 1"));

            verify(branchService, times(1)).getAllBranches();
        }

        /**
         * [HAPPY PATH] MANAGER cũng có quyền xem chi nhánh → 200
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER lấy danh sách → 200")
        void getAllBranches_asManager_shouldReturn200() throws Exception {
            when(branchService.getAllBranches()).thenReturn(List.of(sampleBranch));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        /**
         * [HAPPY PATH] Không có chi nhánh → 200 + []
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Không có chi nhánh → 200 + []")
        void getAllBranches_emptyList_shouldReturn200WithEmpty() throws Exception {
            when(branchService.getAllBranches()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        /**
         * [NEGATIVE] TENANT không có quyền xem chi nhánh → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT không có quyền xem → 403")
        void getAllBranches_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(branchService);
        }

        /**
         * [NEGATIVE] Không có token → 403
         */
        @Test
        @DisplayName("❌ Không có token → 403")
        void getAllBranches_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 2. GET /api/branches/{id}
    // =========================================================
    @Nested
    @DisplayName("GET /api/branches/{id}")
    class GetBranchByIdTests {

        /**
         * [HAPPY PATH] ADMIN lấy chi nhánh theo ID → 200 + đầy đủ detail
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Lấy chi nhánh theo ID → 200 + đầy đủ thông tin")
        void getBranchById_existingBranch_shouldReturn200WithFullDetail() throws Exception {
            when(branchService.getBranchById(1L)).thenReturn(sampleBranch);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.branchCode").value("CN01"))
                    .andExpect(jsonPath("$.data.branchName").value("Chi nhánh Quận 1"));
        }

        /**
         * [NEGATIVE] Chi nhánh ID không tồn tại → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Chi nhánh ID không tồn tại → 500")
        void getBranchById_notFound_shouldReturn500() throws Exception {
            when(branchService.getBranchById(9999L))
                    .thenThrow(new RuntimeException("Branch not found: 9999"));

            mockMvc.perform(get(BASE_URL + "/9999"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================
    // 3. GET /api/branches/code/{branchCode}
    // =========================================================
    @Nested
    @DisplayName("GET /api/branches/code/{branchCode}")
    class GetBranchByCodeTests {

        /**
         * [HAPPY PATH] Lấy chi nhánh theo branchCode → 200
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Lấy chi nhánh theo branchCode → 200")
        void getBranchByCode_validCode_shouldReturn200() throws Exception {
            when(branchService.getBranchByCode("CN01")).thenReturn(sampleBranch);

            mockMvc.perform(get(BASE_URL + "/code/CN01"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.branchCode").value("CN01"));
        }

        /**
         * [NEGATIVE] BranchCode không tồn tại → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ BranchCode không tồn tại → 500")
        void getBranchByCode_notFound_shouldReturn500() throws Exception {
            when(branchService.getBranchByCode("INVALID"))
                    .thenThrow(new RuntimeException("Branch not found: INVALID"));

            mockMvc.perform(get(BASE_URL + "/code/INVALID"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================
    // 4. POST /api/branches – Tạo chi nhánh (ADMIN/DIRECTOR)
    // =========================================================
    @Nested
    @DisplayName("POST /api/branches")
    class CreateBranchTests {

        /**
         * [HAPPY PATH] ADMIN tạo chi nhánh hợp lệ → 201 + data
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN tạo chi nhánh → 201 Created")
        void createBranch_asAdmin_shouldReturn201() throws Exception {
            when(branchService.createBranch(any(BranchRequest.class))).thenReturn(sampleBranch);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Branch created"))
                    .andExpect(jsonPath("$.data.branchCode").value("CN01"))
                    .andExpect(jsonPath("$.data.branchName").value("Chi nhánh Quận 1"));

            verify(branchService, times(1)).createBranch(any(BranchRequest.class));
        }

        /**
         * [HAPPY PATH] DIRECTOR tạo chi nhánh → 201
         */
        @Test
        @WithMockUser(roles = "DIRECTOR")
        @DisplayName("✅ DIRECTOR tạo chi nhánh → 201")
        void createBranch_asDirector_shouldReturn201() throws Exception {
            when(branchService.createBranch(any())).thenReturn(sampleBranch);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }

        /**
         * [NEGATIVE] MANAGER không có quyền tạo chi nhánh → 403
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER tạo chi nhánh → 403 Forbidden")
        void createBranch_asManager_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(branchService);
        }

        /**
         * [NEGATIVE] Tên chi nhánh đã tồn tại → 400 (IllegalArgumentException)
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Tên chi nhánh đã tồn tại → 400")
        void createBranch_duplicateName_shouldReturn400() throws Exception {
            when(branchService.createBranch(any()))
                    .thenThrow(new IllegalArgumentException("Tên chi nhánh đã tồn tại"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    // =========================================================
    // 5. PUT /api/branches/{id} – Cập nhật chi nhánh
    // =========================================================
    @Nested
    @DisplayName("PUT /api/branches/{id}")
    class UpdateBranchTests {

        /**
         * [HAPPY PATH] ADMIN cập nhật chi nhánh → 200 + data mới
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN cập nhật chi nhánh → 200 + data updated")
        void updateBranch_asAdmin_shouldReturn200() throws Exception {
            BranchResponse updated = BranchResponse.builder()
                    .id(1L).branchCode("CN01")
                    .branchName("Chi nhánh Quận 1 (Nâng cấp)")
                    .address("456 Nguyễn Huệ, Q1, HCM")
                    .phoneNumber("02887654321").build();

            when(branchService.updateBranch(eq(1L), any(BranchRequest.class))).thenReturn(updated);

            validRequest.setBranchName("Chi nhánh Quận 1 (Nâng cấp)");

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Branch updated"))
                    .andExpect(jsonPath("$.data.branchName").value("Chi nhánh Quận 1 (Nâng cấp)"));
        }

        /**
         * [NEGATIVE] RECEPTIONIST cập nhật chi nhánh → 403
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("❌ RECEPTIONIST cập nhật chi nhánh → 403")
        void updateBranch_asReceptionist_shouldReturn403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 6. DELETE /api/branches/{id} – Xóa chi nhánh
    // =========================================================
    @Nested
    @DisplayName("DELETE /api/branches/{id}")
    class DeleteBranchTests {

        /**
         * [HAPPY PATH] ADMIN xóa chi nhánh → 200
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN xóa chi nhánh → 200 Branch deleted")
        void deleteBranch_asAdmin_shouldReturn200() throws Exception {
            doNothing().when(branchService).deleteBranch(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Branch deleted"));

            verify(branchService, times(1)).deleteBranch(1L);
        }

        /**
         * [NEGATIVE] DIRECTOR xóa chi nhánh đang hoạt động
         * → IllegalStateException → 400
         */
        @Test
        @WithMockUser(roles = "DIRECTOR")
        @DisplayName("❌ Xóa chi nhánh đang hoạt động → 400")
        void deleteBranch_activeBranch_shouldReturn400() throws Exception {
            doThrow(new IllegalStateException("Không thể xóa chi nhánh đang có phòng"))
                    .when(branchService).deleteBranch(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }

        /**
         * [NEGATIVE] MANAGER không có quyền xóa chi nhánh → 403
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER xóa chi nhánh → 403 Forbidden")
        void deleteBranch_asManager_shouldReturn403() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(branchService);
        }
    }
}
