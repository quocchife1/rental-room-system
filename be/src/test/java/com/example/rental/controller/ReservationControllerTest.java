package com.example.rental.controller;

import com.example.rental.dto.reservation.ReservationRequest;
import com.example.rental.dto.reservation.ReservationResponse;
import com.example.rental.entity.VisitTimeSlot;
import com.example.rental.mapper.ReservationMapper;
import com.example.rental.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite cho ReservationController.
 *
 * Phân quyền cần test:
 *  - POST /api/reservations          → GUEST, TENANT
 *  - PUT  /{id}/confirm              → ADMIN, MANAGER, RECEPTIONIST
 *  - DELETE /{id}                    → GUEST, TENANT, ADMIN, MANAGER, RECEPTIONIST
 *  - GET /{id}                       → GUEST, TENANT, ADMIN, MANAGER, RECEPTIONIST
 *  - GET /my-reservations            → GUEST, TENANT
 *  - GET /room/{roomId}              → ADMIN, MANAGER, RECEPTIONIST
 *  - GET /status/{status}            → ADMIN, MANAGER, RECEPTIONIST
 *  - PUT /{id}/mark-completed        → ADMIN, MANAGER, RECEPTIONIST
 *  - PUT /{id}/mark-no-show          → ADMIN, MANAGER, RECEPTIONIST
 *
 * Chiến lược:
 *  - @MockBean ReservationService, ReservationMapper
 *  - @WithMockUser để giả lập các role khác nhau
 *  - Không chạm DB, không tạo reservation thật
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ReservationController – Integration Tests")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ReservationMapper reservationMapper;

    private static final String BASE_URL        = "/api/reservations";
    private static final String BY_ID_URL       = BASE_URL + "/{id}";
    private static final String CONFIRM_URL     = BASE_URL + "/{id}/confirm";
    private static final String MY_RESV_URL     = BASE_URL + "/my-reservations";
    private static final String BY_ROOM_URL     = BASE_URL + "/room/{roomId}";
    private static final String BY_STATUS_URL   = BASE_URL + "/status/{status}";
    private static final String MARK_DONE_URL   = BASE_URL + "/{id}/mark-completed";
    private static final String NO_SHOW_URL     = BASE_URL + "/{id}/mark-no-show";

    // Fake data dùng chung
    private ReservationResponse sampleReservation;
    private ReservationRequest  validRequest;

    @BeforeEach
    void setUp() {
        // Đăng ký JavaTimeModule để serialize LocalDate, LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // Tạo ReservationResponse mẫu
        sampleReservation = ReservationResponse.builder()
                .id(1L)
                .reservationCode("RES-2025-001")
                .tenantId(10L)
                .tenantName("Nguyen Van Test")
                .tenantEmail("test@example.com")
                .tenantPhoneNumber("0359123456")
                .roomId(5L)
                .roomCode("CN01101")
                .roomNumber("101")
                .status("PENDING")
                .reservationDate(LocalDateTime.now())
                .visitDate(LocalDate.now().plusDays(3))
                .visitSlot("MORNING")
                .notes("Ghi chú test")
                .build();

        // Tạo ReservationRequest hợp lệ
        validRequest = ReservationRequest.builder()
                .roomId(5L)
                .visitDate(LocalDate.now().plusDays(3))
                .visitSlot(VisitTimeSlot.MORNING)
                .notes("Ghi chú test")
                .build();
    }

    // =========================================================
    // 1. POST /api/reservations – Tạo yêu cầu giữ phòng
    // =========================================================
    @Nested
    @DisplayName("POST /api/reservations")
    class CreateReservationTests {

        /**
         * [HAPPY PATH] GUEST tạo yêu cầu giữ phòng → 201 + reservationCode
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("✅ GUEST tạo giữ phòng → 201 + reservationCode")
        void createReservation_asGuest_shouldReturn201() throws Exception {
            when(reservationService.createReservation(any(ReservationRequest.class)))
                    .thenReturn(sampleReservation);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.reservationCode").value("RES-2025-001"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.roomCode").value("CN01101"));

            verify(reservationService, times(1)).createReservation(any(ReservationRequest.class));
        }

        /**
         * [HAPPY PATH] TENANT cũng có quyền tạo giữ phòng → 201
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("✅ TENANT tạo giữ phòng → 201")
        void createReservation_asTenant_shouldReturn201() throws Exception {
            when(reservationService.createReservation(any(ReservationRequest.class)))
                    .thenReturn(sampleReservation);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }

        /**
         * [NEGATIVE] ADMIN không có quyền tạo giữ phòng → 403 Forbidden
         * (chỉ GUEST và TENANT mới được đặt phòng)
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ADMIN tạo giữ phòng → 403 Forbidden")
        void createReservation_asAdmin_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(reservationService);
        }

        /**
         * [NEGATIVE] Thiếu field roomId bắt buộc → 400 Bad Request
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ Thiếu roomId → 400 Bad Request")
        void createReservation_missingRoomId_shouldReturn400() throws Exception {
            // Tạo request thiếu roomId
            String body = """
                    {
                        "visitDate": "2025-12-25",
                        "visitSlot": "MORNING"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(reservationService);
        }

        /**
         * [NEGATIVE] Thiếu visitDate bắt buộc → 400 Bad Request
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ Thiếu visitDate → 400 Bad Request")
        void createReservation_missingVisitDate_shouldReturn400() throws Exception {
            validRequest.setVisitDate(null);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(reservationService);
        }

        /**
         * [NEGATIVE] Thiếu visitSlot bắt buộc → 400 Bad Request
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ Thiếu visitSlot → 400 Bad Request")
        void createReservation_missingVisitSlot_shouldReturn400() throws Exception {
            validRequest.setVisitSlot(null);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(reservationService);
        }

        /**
         * [NEGATIVE] Không có token → 403
         */
        @Test
        @DisplayName("❌ Không có token → 403")
        void createReservation_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 2. PUT /api/reservations/{id}/confirm – Xác nhận giữ phòng
    // =========================================================
    @Nested
    @DisplayName("PUT /api/reservations/{id}/confirm")
    class ConfirmReservationTests {

        /**
         * [HAPPY PATH] RECEPTIONIST xác nhận giữ phòng → 200 + status CONFIRMED
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("✅ RECEPTIONIST xác nhận giữ phòng → 200 + CONFIRMED")
        void confirmReservation_asReceptionist_shouldReturn200() throws Exception {
            ReservationResponse confirmed = ReservationResponse.builder()
                    .id(1L).reservationCode("RES-2025-001").status("CONFIRMED")
                    .roomCode("CN01101").build();

            when(reservationService.confirmReservation(1L)).thenReturn(confirmed);

            mockMvc.perform(put(BASE_URL + "/1/confirm"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

            verify(reservationService, times(1)).confirmReservation(1L);
        }

        /**
         * [HAPPY PATH] MANAGER cũng có quyền xác nhận → 200
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER xác nhận → 200")
        void confirmReservation_asManager_shouldReturn200() throws Exception {
            when(reservationService.confirmReservation(any())).thenReturn(sampleReservation);

            mockMvc.perform(put(BASE_URL + "/1/confirm"))
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] TENANT không có quyền xác nhận → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT xác nhận → 403 Forbidden")
        void confirmReservation_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1/confirm"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(reservationService);
        }

        /**
         * [NEGATIVE] Giữ phòng không tồn tại → 500 (RuntimeException)
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Giữ phòng không tồn tại → 500")
        void confirmReservation_notFound_shouldReturn500() throws Exception {
            when(reservationService.confirmReservation(9999L))
                    .thenThrow(new RuntimeException("Reservation not found: 9999"));

            mockMvc.perform(put(BASE_URL + "/9999/confirm"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================
    // 3. DELETE /api/reservations/{id} – Hủy giữ phòng
    // =========================================================
    @Nested
    @DisplayName("DELETE /api/reservations/{id}")
    class CancelReservationTests {

        /**
         * [HAPPY PATH] GUEST hủy giữ phòng của mình → 200 + message
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("✅ GUEST hủy giữ phòng → 200")
        void cancelReservation_asGuest_shouldReturn200() throws Exception {
            // cancelReservation trả về Reservation (không phải void)
            // Mock trả về null (không dùng mock thật)
            when(reservationService.cancelReservation(1L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Huỷ giữ phòng thành công."));

            verify(reservationService, times(1)).cancelReservation(1L);
        }

        /**
         * [HAPPY PATH] ADMIN cũng có quyền hủy → 200
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ ADMIN hủy giữ phòng → 200")
        void cancelReservation_asAdmin_shouldReturn200() throws Exception {
            when(reservationService.cancelReservation(1L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] Giữ phòng không thể hủy (đã xác nhận)
         * → IllegalStateException → 400
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ Hủy giữ phòng đã xác nhận → 400")
        void cancelReservation_alreadyConfirmed_shouldReturn400() throws Exception {
            doThrow(new IllegalStateException("Không thể hủy giữ phòng đã được xác nhận"))
                    .when(reservationService).cancelReservation(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    // =========================================================
    // 4. GET /api/reservations/{id} – Chi tiết giữ phòng
    // =========================================================
    @Nested
    @DisplayName("GET /api/reservations/{id}")
    class GetReservationByIdTests {

        /**
         * [HAPPY PATH] GUEST lấy chi tiết giữ phòng của mình → 200
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("✅ GUEST lấy chi tiết → 200 + đầy đủ thông tin")
        void getReservationById_asGuest_shouldReturn200() throws Exception {
            when(reservationService.getReservationById(1L)).thenReturn(sampleReservation);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.reservationCode").value("RES-2025-001"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.roomCode").value("CN01101"))
                    .andExpect(jsonPath("$.data.tenantName").value("Nguyen Van Test"));
        }

        /**
         * [NEGATIVE] Giữ phòng không tồn tại → 500
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Giữ phòng không tồn tại → 500")
        void getReservationById_notFound_shouldReturn500() throws Exception {
            when(reservationService.getReservationById(9999L))
                    .thenThrow(new RuntimeException("Reservation not found: 9999"));

            mockMvc.perform(get(BASE_URL + "/9999"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================
    // 5. GET /api/reservations/my-reservations – Giữ phòng của tôi
    // =========================================================
    @Nested
    @DisplayName("GET /api/reservations/my-reservations")
    class GetMyReservationsTests {

        /**
         * [HAPPY PATH] TENANT xem danh sách giữ phòng của mình → 200 + page
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("✅ TENANT xem giữ phòng của mình → 200 + page")
        void getMyReservations_asTenant_shouldReturn200() throws Exception {
            Page<ReservationResponse> mockPage = new PageImpl<>(List.of(sampleReservation));
            when(reservationService.getMyReservations(any(Pageable.class))).thenReturn(mockPage);

            mockMvc.perform(get(BASE_URL + "/my-reservations")
                            .param("page", "0").param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        /**
         * [HAPPY PATH] GUEST cũng có quyền xem → 200
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("✅ GUEST xem giữ phòng của mình → 200")
        void getMyReservations_asGuest_shouldReturn200() throws Exception {
            Page<ReservationResponse> emptyPage = new PageImpl<>(List.of());
            when(reservationService.getMyReservations(any())).thenReturn(emptyPage);

            mockMvc.perform(get(BASE_URL + "/my-reservations"))
                    .andExpect(status().isOk());
        }

        /**
         * [NEGATIVE] ADMIN không được xem /my-reservations → 403
         */
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ ADMIN xem /my-reservations → 403")
        void getMyReservations_asAdmin_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/my-reservations"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verifyNoInteractions(reservationService);
        }
    }

    // =========================================================
    // 6. GET /api/reservations/room/{roomId}
    // =========================================================
    @Nested
    @DisplayName("GET /api/reservations/room/{roomId}")
    class GetByRoomTests {

        /**
         * [HAPPY PATH] RECEPTIONIST lấy danh sách giữ phòng theo phòng → 200
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("✅ RECEPTIONIST lấy danh sách theo phòng → 200")
        void getReservationsByRoom_asReceptionist_shouldReturn200() throws Exception {
            when(reservationService.getReservationsByRoom(5L))
                    .thenReturn(List.of(sampleReservation));

            mockMvc.perform(get(BASE_URL + "/room/5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].roomId").value(5));
        }

        /**
         * [NEGATIVE] GUEST không có quyền xem theo phòng → 403
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ GUEST xem theo phòng → 403")
        void getReservationsByRoom_asGuest_shouldReturn403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/room/5"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 7. PUT /{id}/mark-no-show – Đánh dấu không đến
    // =========================================================
    @Nested
    @DisplayName("PUT /api/reservations/{id}/mark-no-show")
    class MarkNoShowTests {

        /**
         * [HAPPY PATH] RECEPTIONIST đánh dấu khách không đến → 200 + NO_SHOW
         */
        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("✅ RECEPTIONIST đánh dấu no-show → 200 + NO_SHOW")
        void markNoShow_asReceptionist_shouldReturn200() throws Exception {
            ReservationResponse noShow = ReservationResponse.builder()
                    .id(1L).reservationCode("RES-2025-001").status("NO_SHOW").build();

            when(reservationService.markNoShow(1L)).thenReturn(noShow);

            mockMvc.perform(put(BASE_URL + "/1/mark-no-show"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("NO_SHOW"));
        }

        /**
         * [NEGATIVE] TENANT không có quyền đánh dấu no-show → 403
         */
        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT đánh dấu no-show → 403")
        void markNoShow_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1/mark-no-show"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 8. PUT /{id}/mark-completed – Đánh dấu hoàn tất
    // =========================================================
    @Nested
    @DisplayName("PUT /api/reservations/{id}/mark-completed")
    class MarkCompletedTests {

        /**
         * [HAPPY PATH] MANAGER đánh dấu hoàn tất lịch tham khảo → 200
         */
        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ MANAGER đánh dấu completed → 200 + COMPLETED")
        void markCompleted_asManager_shouldReturn200() throws Exception {
            ReservationResponse completed = ReservationResponse.builder()
                    .id(1L).reservationCode("RES-2025-001").status("COMPLETED").build();

            when(reservationService.markCompleted(1L)).thenReturn(completed);

            mockMvc.perform(put(BASE_URL + "/1/mark-completed"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        /**
         * [NEGATIVE] GUEST không có quyền đánh dấu completed → 403
         */
        @Test
        @WithMockUser(roles = "GUEST")
        @DisplayName("❌ GUEST đánh dấu completed → 403")
        void markCompleted_asGuest_shouldReturn403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1/mark-completed"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
