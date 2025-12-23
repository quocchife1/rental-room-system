package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.partner.PartnerResponse;
import com.example.rental.dto.partner.PartnerUpdateProfileRequest;
import com.example.rental.entity.UserStatus;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.PartnerMapper;
import com.example.rental.service.PartnerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management/partners")
@RequiredArgsConstructor
@Tag(name = "Partner Management")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
public class PartnerController {

    private final PartnerService partnerService;
    private final PartnerMapper partnerMapper;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<PartnerResponse>>> getAllPartners() {
        List<PartnerResponse> responses = partnerService.findAllPartners().stream()
                .map(partnerMapper::toResponse)
                .toList();
                
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Danh sách đối tác", 
                responses)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PartnerResponse>> getPartnerById(@PathVariable Long id) {
        PartnerResponse response = partnerService.findById(id)
                .map(partnerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", id));

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Chi tiết đối tác", 
                response)
        );
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponseDto<PartnerResponse>> togglePartnerStatus(@PathVariable Long id) {
        PartnerResponse response = partnerMapper.toResponse(partnerService.toggleStatus(id));

        String message = response.getStatus() == UserStatus.ACTIVE 
                         ? "Đã kích hoạt tài khoản đối tác" 
                         : "Đã khóa tài khoản đối tác";

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                message, 
                response)
        );
    }
    
    /**
     * Cập nhật thông tin hồ sơ đối tác theo ID.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PartnerResponse>> updatePartnerProfile(
            @PathVariable Long id, 
            @Valid @RequestBody PartnerUpdateProfileRequest request) {
        
        PartnerResponse response = partnerService.updatePartnerProfile(id, request);

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Cập nhật hồ sơ đối tác thành công", 
                response)
        );
    }
}