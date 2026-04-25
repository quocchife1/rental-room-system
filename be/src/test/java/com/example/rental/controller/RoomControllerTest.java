package com.example.rental.controller;

import com.example.rental.dto.room.RoomRequest;
import com.example.rental.dto.room.RoomResponse;
import com.example.rental.entity.RoomStatus;
import com.example.rental.service.RoomService;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite cho RoomController.
 *
 * Đặc điểm quan trọng của RoomController:
 *  - GET /api/rooms/**  → permitAll (không cần token, cấu hình trong SecurityConfig)
 *  - POST/PUT/DELETE    → cần role ADMIN hoặc DIRECTOR
 *  - PUT /{id}/status, /{id}/description → thêm MANAGER
 *
 * Chiến lược mock:
 *  - @MockBean RoomService → không chạm DB thật
 *  - @WithMockUser(roles=...) → giả lập phân quyền
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RoomController – Integration Tests")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    // URL Constants
    private static final String BASE_URL        = "/api/rooms";
    private static final String BY_ID_URL       = BASE_URL + "/{id}";
    private static final String BY_CODE_URL     = BASE_URL + "/code/{code}";
    private static final String BY_BRANCH_URL   = BASE_URL + "/branch/{branchCode}";
    private static final String BY_STATUS_URL   = BASE_URL + "/status/{status}";
    private static final String STATUS_URL      = BASE_URL + "/{id}/status";
    private static final String DESC_URL        = BASE_URL + "/{id}/description";

    // Fake data dùng chung
    private RoomResponse sampleRoom;
    private List<RoomResponse> sampleRoomList;
    private RoomRequest validRoomRequest;

    @BeforeEach
    void setUp() {
        // Tạo RoomResponse mẫu – không lấy từ DB
        sampleRoom = RoomResponse.builder()
                .id(1L)
                .roomCode("CN01101")
                .branchCode("CN01")
                .roomNumber("101")
                .area(new BigDecimal("25.5"))
                .price(new BigDecimal("3500000"))
                .status(RoomStatus.AVAILABLE)
                .description("Phòng thoáng mát, view đẹp")
                .images(List.of())
                .build();

        sampleRoomList = List.of(sampleRoom);

        // RoomRequest hợp lệ để tạo/cập nhật
        validRoomRequest = new RoomRequest();
        validRoomRequest.setBranchCode("CN01");
        validRoomRequest.setRoomNumber("101");
        validRoomRequest.setArea(new BigDecimal("25.5"));
        validRoomRequest.setPrice(new BigDecimal("3500000"));
        validRoomRequest.setStatus(RoomStatus.AVAILABLE);
        validRoomRequest.setDescription("Phòng thoáng mát");
    }

    // =========================================================
    // 1. GET /api/rooms – Lấy tất cả phòng (PUBLIC)
    // =========================================================
    @Nested
    @DisplayName("GET /api/rooms")
    class GetAllRoomsTests {

        /**
         * [HAPPY PATH] Lấy tất cả phòng – KHÔNG cần token (permitAll)
         * → 200 + array có dữ liệu
         */
        @Test
        @DisplayName("✅ Lấy tất cả phòng (public) → 200 + list data")
        void getAllRooms_public_shouldReturn200WithList() throws Exception {
            when(roomService.getAllRooms()).thenReturn(sampleRoomList);

            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Rooms fetched"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].roomCode").value("CN01101"))
                    .andExpect(jsonPath("$.data[0].branchCode").value("CN01"))
                    .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));

            verify(roomService, times(1)).getAllRooms();
        }

        /**
         * [HAPPY PATH] Danh sách rỗng khi không có phòng nào → 200 + []
         */
        @Test
        @DisplayName("✅ Không có phòng nào → 200 + data = []")
        void getAllRooms_emptyList_shouldReturn200WithEmptyArray() throws Exception {
            when(roomService.getAllRooms()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    // =========================================================
    // 2. GET /api/rooms/{id}
    // =========================================================
    @Nested
    @DisplayName("GET /api/rooms/{id}")
    class GetRoomByIdTests {

        /**
         * [HAPPY PATH] Lấy phòng theo ID tồn tại → 200 + đầy đủ fields
         */
        @Test
        @DisplayName("✅ Lấy phòng theo ID → 200 + đầy đủ thông tin")
        void getRoomById_existingRoom_shouldReturn200() throws Exception {
            when(roomService.getRoomById(1L)).thenReturn(sampleRoom);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.roomCode").value("CN01101"))
                    .andExpect(jsonPath("$.data.roomNumber").value("101"))
                    .andExpect(jsonPath("$.data.price").value(3500000))
                    .andExpect(jsonPath("$.data.area").value(25.5));
        }

        /**
         * [NEGATIVE] Phòng không tồn tại → 500 (RuntimeException)
         */
        @Test
        @DisplayName("❌ Phòng ID không tồn tại → 500 Internal Server Error")
        void getRoomById_notFound_shouldReturn500() throws Exception {
            when(roomService.getRoomById(9999L))
                    .thenThrow(new RuntimeException("Room not found: 9999"));

            mockMvc.perform(get(BASE_URL + "/9999"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.statusCode").value(500));
        }
    }

    // =========================================================
    // 3. GET /api/rooms/code/{roomCode}
    // =========================================================
    @Nested
    @DisplayName("GET /api/rooms/code/{roomCode}")
    class GetRoomByCodeTests {

        /**
         * [HAPPY PATH] Lấy phòng theo roomCode hợp lệ → 200
         */
        @Test
        @DisplayName("✅ Lấy phòng theo roomCode → 200")
        void getRoomByCode_validCode_shouldReturn200() throws Exception {
            when(roomService.getRoomByCode("CN01101")).thenReturn(sampleRoom);

            mockMvc.perform(get(BASE_URL + "/code/CN01101"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.roomCode").value("CN01101"));
        }

        /**
         * [NEGATIVE] roomCode không tồn tại → 500
         */
        @Test
        @DisplayName("❌ roomCode không tồn tại → 500")
        void getRoomByCode_notFound_shouldReturn500() throws Exception {
            when(roomService.getRoomByCode("INVALID_CODE"))
                    .thenThrow(new RuntimeException("Room not found: INVALID_CODE"));

            mockMvc.perform(get(BASE_URL + "/code/INVALID_CODE"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================
    // 4. GET /api/rooms/branch/{branchCode}
    // =========================================================
    @Nested
    @DisplayName("GET /api/rooms/branch/{branchCode}")
    class GetRoomsByBranchTests {

        /**
         * [HAPPY PATH] Lấy phòng theo branchCode → 200 + list
         */
        @Test
        @DisplayName("✅ Lấy phòng theo branchCode → 200 + có data")
        void getRoomsByBranchCode_validBranch_shouldReturn200() throws Exception {
            when(roomService.getRoomsByBranchCode("CN01")).thenReturn(sampleRoomList);

            mockMvc.perform(get(BASE_URL + "/branch/CN01"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].branchCode").value("CN01"));
        }

        /**
         * [HAPPY PATH] Branch không có phòng → 200 + []
         */
        @Test
        @DisplayName("✅ Branch không có phòng → 200 + []")
        void getRoomsByBranchCode_emptyBranch_shouldReturn200WithEmpty() throws Exception {
            when(roomService.getRoomsByBranchCode("CN99")).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/branch/CN99"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    // =========================================================
    // 5. GET /api/rooms/status/{status}
    // =========================================================
    @Nested
    @DisplayName("GET /api/rooms/status/{status}")
    class GetRoomsByStatusTests {

        /**
         * [HAPPY PATH] AVAILABLE rooms → 200 + list
         */
        @Test
        @DisplayName("✅ Lấy phòng AVAILABLE → 200 + có data")
        void getRoomsByStatus_available_shouldReturn200() throws Exception {
            when(roomService.getRoomsByStatus(RoomStatus.AVAILABLE)).thenReturn(sampleRoomList);

            mockMvc.perform(get(BASE_URL + "/status/AVAILABLE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));
        }

        /**
         * [HAPPY PATH] MAINTENANCE rooms → 200 + list (có thể rỗng)
         */
        @Test
        @DisplayName("✅ Lấy phòng MAINTENANCE → 200")
        void getRoomsByStatus_maintenance_shouldReturn200() throws Exception {
            when(roomService.getRoomsByStatus(RoomStatus.MAINTENANCE)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/status/MAINTENANCE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // =========================================================
    // 6. POST /api/rooms – Tạo phòng (cần ADMIN/DIRECTOR)
    // =========================================================
    @Nested
    @DisplayName("POST /api/rooms")
    class CreateRoomTests {

        /**
         * [HAPPY PATH] ADMIN tạo phòng hợp lệ → 201 Created
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN tạo phòng → 201 Created")
        void createRoom_asAdmin_shouldReturn201() throws Exception {
            when(roomService.createRoom(any(RoomRequest.class))).thenReturn(sampleRoom);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRoomRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Room created"))
                    .andExpect(jsonPath("$.data.roomCode").value("CN01101"))
                    .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

            verify(roomService, times(1)).createRoom(any(RoomRequest.class));
        }

        /**
         * [HAPPY PATH] DIRECTOR cũng có quyền tạo phòng → 201
         */
        @Test
        @WithMockUser(roles = "DIRECTOR")
        @DisplayName("✅ DIRECTOR tạo phòng → 201 Created")
        void createRoom_asDirector_shouldReturn201() throws Exception {
            when(roomService.createRoom(any())).thenReturn(sampleRoom);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRoomRequest)))
                    .andExpect(status().isCreated());
        }

        /**
         * [NEGATIVE] MANAGER không có quyền tạo phòng → 403 Forbidden
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("❌ MANAGER không có quyền tạo phòng → 403")
        void createRoom_asManager_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRoomRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(roomService);
        }

        /**
         * [NEGATIVE] Không có token → 403 (Spring Security default)
         */
        @Test
        @DisplayName("❌ Không có token → 403")
        void createRoom_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRoomRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(roomService);
        }
    }

    // =========================================================
    // 7. PUT /api/rooms/{id} – Cập nhật phòng (ADMIN/DIRECTOR)
    // =========================================================
    @Nested
    @DisplayName("PUT /api/rooms/{id}")
    class UpdateRoomTests {

        /**
         * [HAPPY PATH] ADMIN cập nhật phòng → 200 + updated data
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN cập nhật phòng → 200 + data updated")
        void updateRoom_asAdmin_shouldReturn200() throws Exception {
            RoomResponse updated = RoomResponse.builder()
                    .id(1L).roomCode("CN01101").branchCode("CN01")
                    .roomNumber("101").area(new BigDecimal("30.0"))
                    .price(new BigDecimal("4000000")).status(RoomStatus.AVAILABLE)
                    .description("Mô tả mới").images(List.of()).build();

            when(roomService.updateRoom(eq(1L), any(RoomRequest.class))).thenReturn(updated);

            validRoomRequest.setArea(new BigDecimal("30.0"));
            validRoomRequest.setPrice(new BigDecimal("4000000"));

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRoomRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Room updated"))
                    .andExpect(jsonPath("$.data.price").value(4000000))
                    .andExpect(jsonPath("$.data.area").value(30.0));
        }

        /**
         * [NEGATIVE] TENANT không được cập nhật phòng → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT cập nhật phòng → 403")
        void updateRoom_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRoomRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 8. PUT /api/rooms/{id}/status – Cập nhật trạng thái phòng
    // =========================================================
    @Nested
    @DisplayName("PUT /api/rooms/{id}/status")
    class UpdateRoomStatusTests {

        /**
         * [HAPPY PATH] MANAGER đổi trạng thái phòng sang MAINTENANCE → 200
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER đổi status → MAINTENANCE → 200")
        void updateRoomStatus_toMaintenance_shouldReturn200() throws Exception {
            RoomResponse maintenanceRoom = RoomResponse.builder()
                    .id(1L).roomCode("CN01101").status(RoomStatus.MAINTENANCE)
                    .branchCode("CN01").roomNumber("101").images(List.of()).build();

            when(roomService.updateRoomStatus(eq(1L), eq(RoomStatus.MAINTENANCE)))
                    .thenReturn(maintenanceRoom);

            mockMvc.perform(put(BASE_URL + "/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"MAINTENANCE\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("MAINTENANCE"));
        }

        /**
         * [NEGATIVE] Body thiếu field status → 400 Bad Request
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Thiếu trường status → 400 Bad Request")
        void updateRoomStatus_missingStatus_shouldReturn400() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        /**
         * [NEGATIVE] Status không hợp lệ (không thuộc enum) → 400
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Status không hợp lệ → 400 Bad Request")
        void updateRoomStatus_invalidStatus_shouldReturn400() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"INVALID_STATUS\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }

        /**
         * [NEGATIVE] GUEST không có quyền đổi status → 403
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ GUEST đổi status → 403 Forbidden")
        void updateRoomStatus_asGuest_shouldReturn403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"MAINTENANCE\"}"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 9. DELETE /api/rooms/{id} – Xóa phòng (ADMIN/DIRECTOR)
    // =========================================================
    @Nested
    @DisplayName("DELETE /api/rooms/{id}")
    class DeleteRoomTests {

        /**
         * [HAPPY PATH] ADMIN xóa phòng → 200
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN xóa phòng → 200 Room deleted")
        void deleteRoom_asAdmin_shouldReturn200() throws Exception {
            doNothing().when(roomService).deleteRoom(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Room deleted"));

            verify(roomService, times(1)).deleteRoom(1L);
        }

        /**
         * [NEGATIVE] RECEPTIONIST không có quyền xóa → 403
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("❌ RECEPTIONIST xóa phòng → 403 Forbidden")
        void deleteRoom_asReceptionist_shouldReturn403() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(roomService);
        }

        /**
         * [NEGATIVE] Phòng không tồn tại → Service throw RuntimeException → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Xóa phòng không tồn tại → 500")
        void deleteRoom_notFound_shouldReturn500() throws Exception {
            doThrow(new RuntimeException("Room not found: 9999"))
                    .when(roomService).deleteRoom(9999L);

            mockMvc.perform(delete(BASE_URL + "/9999"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================
    // 10. PUT /api/rooms/{id}/description
    // =========================================================
    @Nested
    @DisplayName("PUT /api/rooms/{id}/description")
    class UpdateDescriptionTests {

        /**
         * [HAPPY PATH] MANAGER cập nhật mô tả phòng → 200
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER cập nhật mô tả → 200 + description mới")
        void updateDescription_asManager_shouldReturn200() throws Exception {
            RoomResponse updated = RoomResponse.builder()
                    .id(1L).roomCode("CN01101").description("Mô tả cập nhật mới")
                    .branchCode("CN01").roomNumber("101").images(List.of()).build();

            when(roomService.updateRoomDescription(eq(1L), any())).thenReturn(updated);

            mockMvc.perform(put(BASE_URL + "/1/description")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"description\": \"Mô tả cập nhật mới\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Room description updated"))
                    .andExpect(jsonPath("$.data.description").value("Mô tả cập nhật mới"));
        }
    }
}
