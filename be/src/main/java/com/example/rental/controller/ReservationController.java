package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.reservation.ReservationRequest;
import com.example.rental.dto.reservation.ReservationResponse;
import com.example.rental.dto.contract.ContractPrefillResponse;
import com.example.rental.mapper.ReservationMapper;
import com.example.rental.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Reservation Management", description = "Quản lý giữ phòng (Đặt phòng trực tuyến)")
public class ReservationController {
    
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;
    
    /**
     * Người thuê tạo yêu cầu giữ phòng (Online Booking)
     */
        @Operation(summary = "Tạo yêu cầu giữ phòng (Dành cho Guest & Tenant)")
    @PostMapping
    @PreAuthorize("hasAnyRole('GUEST', 'TENANT')") // Cho phép cả Guest đặt phòng
    public ResponseEntity<ApiResponseDto<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), 
                        "Yêu cầu giữ phòng đã được gửi thành công!", response));
    }
    
    /**
     * Nhân viên Sales xác nhận giữ phòng
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Xác nhận giữ phòng (Nhân viên Sales)")
    public ResponseEntity<ApiResponseDto<ReservationResponse>> confirmReservation(
            @PathVariable Long id) {
        ReservationResponse response = reservationService.confirmReservation(id);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Xác nhận giữ phòng thành công. Phòng đã được giữ lại.",
                response
        ));
    }
    
    /**
     * Huỷ yêu cầu giữ phòng
     */
        @DeleteMapping("/{id}")
        // Allow both Tenant and Guest to cancel their own reservation (plus staff roles)
        @PreAuthorize("hasAnyRole('GUEST', 'TENANT', 'ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Huỷ yêu cầu giữ phòng")
    public ResponseEntity<ApiResponseDto<Void>> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Huỷ giữ phòng thành công.",
                null
        ));
    }
    
    /**
     * Lấy chi tiết yêu cầu giữ phòng
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT', 'GUEST', 'ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Lấy chi tiết giữ phòng theo ID")
    public ResponseEntity<ApiResponseDto<ReservationResponse>> getReservationById(
            @PathVariable Long id) {
        ReservationResponse response = reservationService.getReservationById(id);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Lấy chi tiết giữ phòng thành công.",
                response
        ));
    }
    
    /**
     * Lấy danh sách giữ phòng của người thuê hiện tại
     */
@Operation(summary = "Lấy danh sách giữ phòng của tôi")
    @GetMapping("/my-reservations")
    @PreAuthorize("hasAnyRole('GUEST', 'TENANT')")
    public ResponseEntity<ApiResponseDto<Page<ReservationResponse>>> getMyReservations(Pageable pageable) {
                // Debug log: who is calling
                try {
                        String current = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getName() : "<no-auth>";
                        log.debug("GET /api/reservations/my-reservations called by: {}", current);
                } catch (Exception ex) {
                        // ignore
                }

                Page<ReservationResponse> response = reservationService.getMyReservations(pageable);
                log.debug("Returning {} reservations (page size {})", response == null ? 0 : response.getNumberOfElements(), pageable.getPageSize());
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Danh sách giữ phòng của bạn", response));
    }
        
    
    /**
     * Lấy danh sách giữ phòng theo phòng
     */
    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Lấy danh sách giữ phòng theo phòng")
    public ResponseEntity<ApiResponseDto<List<ReservationResponse>>> getReservationsByRoom(
            @PathVariable Long roomId) {
        List<ReservationResponse> response = reservationService.getReservationsByRoom(roomId);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Lấy danh sách giữ phòng thành công.",
                response
        ));
    }
    
    /**
     * Lấy danh sách giữ phòng theo trạng thái
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Lấy danh sách giữ phòng theo trạng thái")
    public ResponseEntity<ApiResponseDto<Page<ReservationResponse>>> getReservationsByStatus(
            @PathVariable String status,
            Pageable pageable) {
        Page<ReservationResponse> response = reservationService.getReservationsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Lấy danh sách giữ phòng thành công.",
                response
        ));
    }

        /**
         * Lễ tân tra cứu phiếu đặt theo mã/khách/phòng
         */
        @GetMapping("/search")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
        @Operation(summary = "Tra cứu phiếu giữ phòng")
        public ResponseEntity<ApiResponseDto<Page<ReservationResponse>>> search(
                        @RequestParam(name = "q") String q,
                        Pageable pageable) {
                Page<ReservationResponse> response = reservationService.searchReservations(q, pageable);
                return ResponseEntity.ok(ApiResponseDto.success(
                                HttpStatus.OK.value(),
                                "Kết quả tra cứu giữ phòng",
                                response
                ));
        }

            /**
             * Lễ tân xem danh sách phiếu trong chi nhánh của mình (mặc định: tất cả trạng thái)
             */
            @GetMapping("/my-branch")
            @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
            @Operation(summary = "Danh sách giữ phòng theo chi nhánh của tôi")
            public ResponseEntity<ApiResponseDto<Page<ReservationResponse>>> getMyBranchReservations(
                    @RequestParam(name = "status", required = false) String status,
                    @RequestParam(name = "q", required = false) String q,
                    Pageable pageable) {
                Page<ReservationResponse> response = reservationService.getMyBranchReservations(status, q, pageable);
                return ResponseEntity.ok(ApiResponseDto.success(
                        HttpStatus.OK.value(),
                        "Danh sách giữ phòng theo chi nhánh",
                        response
                ));
            }

        /**
         * Lễ tân đánh dấu hoàn tất lịch tham khảo (khách xem xong nhưng không thuê)
         */
        @PutMapping("/{id}/mark-completed")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
        @Operation(summary = "Đánh dấu hoàn tất lịch tham khảo")
        public ResponseEntity<ApiResponseDto<ReservationResponse>> markCompleted(@PathVariable Long id) {
                ReservationResponse response = reservationService.markCompleted(id);
                return ResponseEntity.ok(ApiResponseDto.success(
                                HttpStatus.OK.value(),
                                "Đã cập nhật trạng thái hoàn tất.",
                                response
                ));
        }

        /**
         * Lễ tân đánh dấu khách không đến
         */
        @PutMapping("/{id}/mark-no-show")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
        @Operation(summary = "Đánh dấu khách không đến")
        public ResponseEntity<ApiResponseDto<ReservationResponse>> markNoShow(@PathVariable Long id) {
                ReservationResponse response = reservationService.markNoShow(id);
                return ResponseEntity.ok(ApiResponseDto.success(
                                HttpStatus.OK.value(),
                                "Đã cập nhật trạng thái không đến.",
                                response
                ));
        }
    
    /**
     * Chuyển giữ phòng thành hợp đồng (sau khi khách thanh toán deposit)
     */
    @PostMapping("/{id}/convert-to-contract")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    @Operation(summary = "Chuyển giữ phòng thành hợp đồng")
    public ResponseEntity<ApiResponseDto<Long>> convertReservationToContract(
            @PathVariable Long id) {
        Long contractId = reservationService.convertReservationToContract(id);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Chuyển giữ phòng thành hợp đồng thành công.",
                contractId
        ));
    }

        /**
         * Lấy dữ liệu prefill để lập hợp đồng (không tạo hợp đồng ngay)
         */
        @GetMapping("/{id}/contract-prefill")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
        @Operation(summary = "Prefill dữ liệu lập hợp đồng từ phiếu giữ phòng")
        public ResponseEntity<ApiResponseDto<ContractPrefillResponse>> getContractPrefill(@PathVariable Long id) {
                ContractPrefillResponse response = reservationService.getContractPrefill(id);
                return ResponseEntity.ok(ApiResponseDto.success(
                                HttpStatus.OK.value(),
                                "Dữ liệu prefill lập hợp đồng",
                                response
                ));
        }

        /**
         * Đánh dấu phiếu giữ phòng đã lập hợp đồng (không giải phóng phòng)
         */
        @PutMapping("/{id}/mark-contracted")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
        @Operation(summary = "Đánh dấu phiếu giữ phòng đã lập hợp đồng")
        public ResponseEntity<ApiResponseDto<ReservationResponse>> markContracted(@PathVariable Long id) {
                ReservationResponse response = reservationService.markContracted(id);
                return ResponseEntity.ok(ApiResponseDto.success(
                                HttpStatus.OK.value(),
                                "Đã đánh dấu phiếu giữ phòng đã lập hợp đồng.",
                                response
                ));
        }
}
