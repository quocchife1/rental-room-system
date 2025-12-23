package com.example.rental.controller;

import com.example.rental.dto.rentalservice.RentalServiceRequest;
import com.example.rental.dto.rentalservice.RentalServiceResponse;
import com.example.rental.service.RentalServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Rental Service API", description = "Quản lý các dịch vụ nhà trọ")
@SecurityRequirement(name = "Bearer Authentication")
public class RentalServiceController {

    private final RentalServiceService rentalServiceService;

    @Operation(summary = "Tạo dịch vụ mới")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RentalServiceResponse>> create(@RequestBody RentalServiceRequest request) {
        RentalServiceResponse resp = rentalServiceService.create(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(com.example.rental.dto.ApiResponseDto.success(201, "Service created", resp));
    }

    @Operation(summary = "Cập nhật dịch vụ theo ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RentalServiceResponse>> update(
            @PathVariable Long id,
            @RequestBody RentalServiceRequest request
    ) {
        RentalServiceResponse resp = rentalServiceService.update(id, request);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Service updated", resp));
    }

    @Operation(summary = "Xoá dịch vụ theo ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<Void>> delete(@PathVariable Long id) {
        rentalServiceService.delete(id);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Service deleted"));
    }

    @Operation(summary = "Lấy dịch vụ theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT','RECEPTIONIST','MAINTENANCE','SECURITY')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RentalServiceResponse>> getById(@PathVariable Long id) {
        RentalServiceResponse resp = rentalServiceService.getById(id);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Service fetched", resp));
    }

    @Operation(summary = "Lấy toàn bộ dịch vụ")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT','RECEPTIONIST','MAINTENANCE','SECURITY')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<RentalServiceResponse>>> getAll() {
        java.util.List<RentalServiceResponse> list = rentalServiceService.getAll();
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Services fetched", list));
    }
}
