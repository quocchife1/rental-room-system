package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.booking.CancelBookingRequest;
import com.example.rental.dto.booking.CleaningBookingManagerRow;
import com.example.rental.dto.booking.CreateCleaningBookingRequest;
import com.example.rental.dto.booking.ServiceBookingResponse;
import com.example.rental.service.ServiceBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Service Booking API", description = "Đăng ký dịch vụ theo lịch")
public class ServiceBookingController {

    private final ServiceBookingService serviceBookingService;

    @Operation(summary = "Tenant đăng ký vệ sinh tuần (Thứ 5 08:00-11:00)")
    @PostMapping("/api/contracts/{contractId}/bookings/cleaning")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<ServiceBookingResponse>> createCleaning(
            @PathVariable Long contractId,
            @RequestBody(required = false) CreateCleaningBookingRequest request
    ) {
        var resp = serviceBookingService.createNextCleaningBooking(contractId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(201, "Đăng ký vệ sinh thành công", resp));
    }

    @Operation(summary = "Danh sách lịch dịch vụ của hợp đồng")
    @GetMapping("/api/contracts/{contractId}/bookings")
    @PreAuthorize("hasAnyRole('TENANT','ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<List<ServiceBookingResponse>>> list(
            @PathVariable Long contractId
    ) {
        var resp = serviceBookingService.listBookingsForContract(contractId);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Danh sách lịch", resp));
    }

    @Operation(summary = "Manager xác nhận vệ sinh đã hoàn thành")
    @PostMapping("/api/bookings/{bookingId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponseDto<ServiceBookingResponse>> complete(
            @PathVariable Long bookingId
    ) {
        var resp = serviceBookingService.markCompleted(bookingId);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã xác nhận hoàn thành", resp));
    }

    @Operation(summary = "Manager xem danh sách lịch vệ sinh của chi nhánh mình")
    @GetMapping("/api/bookings/cleaning/my-branch")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponseDto<List<CleaningBookingManagerRow>>> listCleaningMyBranch() {
        var resp = serviceBookingService.listCleaningBookingsMyBranch();
        return ResponseEntity.ok(ApiResponseDto.success(200, "Danh sách lịch vệ sinh", resp));
    }

    @Operation(summary = "Manager hủy lịch vệ sinh (kèm lý do)")
    @PostMapping("/api/bookings/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponseDto<ServiceBookingResponse>> cancel(
            @PathVariable Long bookingId,
            @RequestBody CancelBookingRequest request
    ) {
        var resp = serviceBookingService.cancelBookingAsManager(bookingId, request != null ? request.getReason() : null);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã hủy lịch", resp));
    }
}
