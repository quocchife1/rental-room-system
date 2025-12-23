package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.contractservice.ContractServiceRequest;
import com.example.rental.dto.contractservice.ContractServiceResponse;
import com.example.rental.service.ContractServiceService;
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
@RequestMapping("/api/contracts/{contractId}/services")
@RequiredArgsConstructor
@Tag(name = "Contract Services", description = "Quản lý dịch vụ gắn với hợp đồng")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractServiceController {

    private final ContractServiceService contractServiceService;

    @Operation(summary = "Thêm dịch vụ vào hợp đồng")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ACCOUNTANT','RECEPTIONIST','TENANT')")
    public ResponseEntity<ApiResponseDto<ContractServiceResponse>> addService(
            @PathVariable Long contractId,
            @RequestBody ContractServiceRequest request) {
        var result = contractServiceService.addServiceToContract(contractId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Thêm dịch vụ thành công", result));
    }

    @Operation(summary = "Lấy tất cả dịch vụ của hợp đồng")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ACCOUNTANT','RECEPTIONIST','TENANT')")
    public ResponseEntity<ApiResponseDto<List<ContractServiceResponse>>> getServices(
            @PathVariable Long contractId) {
        var result = contractServiceService.getServicesByContract(contractId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Danh sách dịch vụ", result));
    }

    @Operation(summary = "Xóa dịch vụ khỏi hợp đồng")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<Void>> removeService(@PathVariable Long id) {
        contractServiceService.removeService(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(), "Dịch vụ đã được xóa"));
    }

    @Operation(summary = "Hủy dịch vụ (hiệu lực cuối tháng)")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<ContractServiceResponse>> cancelService(
            @PathVariable Long contractId,
            @PathVariable Long id
    ) {
        var resp = contractServiceService.cancelServiceEffectiveEndOfMonth(contractId, id);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Hủy dịch vụ thành công (hiệu lực cuối tháng)", resp));
    }

    @Operation(summary = "Cập nhật chỉ số điện/nước (Manager)")
    @PutMapping("/{id}/meter-reading")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponseDto<ContractServiceResponse>> updateMeterReading(
            @PathVariable Long contractId,
            @PathVariable Long id,
            @RequestBody com.example.rental.dto.contractservice.MeterReadingUpdateRequest request
    ) {
        var resp = contractServiceService.updateMeterReadings(contractId, id, request);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Cập nhật chỉ số thành công", resp));
    }
}
