package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.service.DamageReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/damage-reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Damage Report Management", description = "Quản lý báo cáo hư hỏng phòng khi trả phòng")
public class DamageReportController {

        private final DamageReportService damageReportService;
        private final ObjectMapper objectMapper;

        @Operation(summary = "Tạo báo cáo hư hỏng mới (DRAFT)")
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE')")
        public ResponseEntity<ApiResponseDto<DamageReportResponse>> createDamageReport(
                        @RequestPart("damageReport") String damageReportJson,
                        @RequestParam(value = "contractId", required = false) Long contractId,
                        @RequestPart(value = "images", required = false) List<MultipartFile> images)
                        throws IOException {

                DamageReportCreateRequest request;
                try {
                        request = objectMapper.readValue(damageReportJson, DamageReportCreateRequest.class);
                } catch (Exception ex) {
                        // Nếu client gửi plain text (ví dụ: chỉ gửi mô tả), dùng giá trị đó làm
                        // description
                        request = new DamageReportCreateRequest();
                        request.setDescription(damageReportJson);
                        if (contractId != null) {
                                request.setContractId(contractId);
                        }
                }

                DamageReportResponse response = damageReportService.createDamageReport(request, images);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                                                "Tạo báo cáo hư hỏng thành công", response));
        }

        @Operation(summary = "Lấy báo cáo hư hỏng theo ID")
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE', 'ACCOUNTANT')")
        public ResponseEntity<ApiResponseDto<DamageReportResponse>> getDamageReportById(@PathVariable Long id) {
                DamageReportResponse response = damageReportService.getById(id);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Lấy báo cáo hư hỏng thành công", response));
        }

        @Operation(summary = "Lấy tất cả báo cáo hư hỏng của một hợp đồng")
        @GetMapping("/contract/{contractId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE', 'ACCOUNTANT')")
        public ResponseEntity<ApiResponseDto<List<DamageReportResponse>>> getDamageReportsByContractId(
                        @PathVariable Long contractId) {
                List<DamageReportResponse> responses = damageReportService.getByContractId(contractId);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Lấy danh sách báo cáo thành công", responses));
        }

        @Operation(summary = "Lấy tất cả báo cáo hư hỏng theo trạng thái")
        @GetMapping("/status/{status}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE', 'ACCOUNTANT')")
        public ResponseEntity<ApiResponseDto<List<DamageReportResponse>>> getDamageReportsByStatus(
                        @PathVariable String status) {
                List<DamageReportResponse> responses = damageReportService.getByStatus(status);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Lấy danh sách báo cáo theo trạng thái thành công", responses));
        }

        @Operation(summary = "Lấy tất cả báo cáo hư hỏng")
        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE', 'ACCOUNTANT')")
        public ResponseEntity<ApiResponseDto<List<DamageReportResponse>>> getAllDamageReports() {
                List<DamageReportResponse> responses = damageReportService.getAll();
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Lấy tất cả báo cáo hư hỏng thành công", responses));
        }

        @Operation(summary = "Cập nhật báo cáo hư hỏng (DRAFT)")
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE')")
        public ResponseEntity<ApiResponseDto<DamageReportResponse>> updateDamageReport(
                        @PathVariable Long id,
                        @RequestPart("damageReport") String damageReportJson,
                        @RequestPart(value = "images", required = false) List<MultipartFile> newImages)
                        throws IOException {

                DamageReportCreateRequest request;
                try {
                        request = objectMapper.readValue(damageReportJson, DamageReportCreateRequest.class);
                } catch (Exception ex) {
                        request = new DamageReportCreateRequest();
                        request.setDescription(damageReportJson);
                }
                DamageReportResponse response = damageReportService.updateDamageReport(id, request, newImages);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Cập nhật báo cáo hư hỏng thành công", response));
        }

        @Operation(summary = "Gửi báo cáo để duyệt (DRAFT -> SUBMITTED)")
        @PostMapping("/{id}/submit")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE')")
        public ResponseEntity<ApiResponseDto<DamageReportResponse>> submitForApproval(@PathVariable Long id) {
                DamageReportResponse response = damageReportService.submitForApproval(id);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Gửi báo cáo để duyệt thành công", response));
        }

        @Operation(summary = "Phê duyệt báo cáo hư hỏng (SUBMITTED -> APPROVED)")
        @PostMapping("/{id}/approve")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public ResponseEntity<ApiResponseDto<DamageReportResponse>> approveDamageReport(
                        @PathVariable Long id,
                        @RequestParam(required = false) String approverNote) {

                DamageReportResponse response = damageReportService.approveDamageReport(id, approverNote);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Phê duyệt báo cáo hư hỏng thành công", response));
        }

        @Operation(summary = "Từ chối báo cáo hư hỏng (SUBMITTED -> REJECTED)")
        @PostMapping("/{id}/reject")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public ResponseEntity<ApiResponseDto<DamageReportResponse>> rejectDamageReport(
                        @PathVariable Long id,
                        @RequestParam(required = false) String rejectReason) {

                DamageReportResponse response = damageReportService.rejectDamageReport(id, rejectReason);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Từ chối báo cáo hư hỏng thành công", response));
        }

        @Operation(summary = "Xóa báo cáo hư hỏng (chỉ DRAFT)")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MAINTENANCE')")
        public ResponseEntity<ApiResponseDto<Void>> deleteDamageReport(@PathVariable Long id) {
                damageReportService.deleteDamageReport(id);
                return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                                "Xóa báo cáo hư hỏng thành công", null));
        }

        // NOTE: API for uploading additional damage images was removed intentionally.
        // Use create/update endpoints which accept images as part of the multipart request.
}
