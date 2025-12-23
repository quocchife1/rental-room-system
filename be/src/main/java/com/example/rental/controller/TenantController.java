package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.tenant.TenantResponse;
import com.example.rental.dto.tenant.TenantUpdateProfileRequest;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.TenantMapper;
import com.example.rental.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.rental.entity.UserStatus;

import java.util.List;

@RestController
@RequestMapping("/api/management/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<TenantResponse>>> getAllTenants() {
        List<TenantResponse> responses = tenantService.findAllTenants().stream()
                .map(tenantMapper::tenantToTenantResponse)
                .toList();
                
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Danh sách người thuê", 
                responses)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TenantResponse>> getTenantById(@PathVariable Long id) {
        TenantResponse response = tenantService.findById(id)
                .map(tenantMapper::tenantToTenantResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", id));

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Chi tiết người thuê", 
                response)
        );
    }
    
    /**
     * Cập nhật thông tin hồ sơ người thuê theo ID.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TenantResponse>> updateTenantProfile(
            @PathVariable Long id, 
            @Valid @RequestBody TenantUpdateProfileRequest request) {
        
        TenantResponse response = tenantService.updateTenantProfile(id, request);

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Cập nhật hồ sơ người thuê thành công", 
                response)
        );
    }

        /**
         * Cập nhật trạng thái (Kích hoạt/Khóa) của người thuê
         */
        @PatchMapping("/{id}/status")
        @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
        public ResponseEntity<ApiResponseDto<TenantResponse>> updateTenantStatus(
                        @PathVariable Long id,
                        @RequestParam UserStatus status) {
                if (status != UserStatus.ACTIVE && status != UserStatus.BANNED) {
                        throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận ACTIVE hoặc BANNED.");
                }
                TenantResponse response = tenantService.updateStatus(id, status);
                return ResponseEntity.ok(ApiResponseDto.success(
                                HttpStatus.OK.value(),
                                "Cập nhật trạng thái người thuê thành công",
                                response)
                );
        }
}