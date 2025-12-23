package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.checkout.CheckoutRequestManagerRow;
import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.dto.invoice.InvoiceResponse;
import com.example.rental.service.CheckoutManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/checkout-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Checkout Manager", description = "Quản lý yêu cầu trả phòng và biên bản kiểm tra")
public class CheckoutManagerController {

    private final CheckoutManagerService checkoutManagerService;

    @Operation(summary = "Danh sách yêu cầu trả phòng theo chi nhánh của quản lý")
    @GetMapping("/my-branch")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDto<Page<CheckoutRequestManagerRow>>> listMyBranch(
            @RequestParam(name = "status", required = false) List<String> statuses,
            Pageable pageable
    ) {
        Page<CheckoutRequestManagerRow> page = checkoutManagerService.listMyBranchRequests(statuses, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Danh sách yêu cầu trả phòng", page));
    }

    @Operation(summary = "Duyệt yêu cầu trả phòng")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDto<CheckoutRequestManagerRow>> approve(@PathVariable Long id) {
        var row = checkoutManagerService.approve(id);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã duyệt yêu cầu trả phòng", row));
    }

    @Operation(summary = "Lấy hoặc tạo biên bản kiểm tra trả phòng (DamageReport)")
    @GetMapping("/{id}/inspection-report")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDto<DamageReportResponse>> getOrCreateReport(@PathVariable Long id) {
        DamageReportResponse resp = checkoutManagerService.getOrCreateInspectionReport(id);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Biên bản kiểm tra", resp));
    }

    @Operation(summary = "Lưu biên bản kiểm tra trả phòng")
    @PutMapping("/{id}/inspection-report")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDto<DamageReportResponse>> saveReport(
            @PathVariable Long id,
            @RequestBody DamageReportCreateRequest request
    ) {
        DamageReportResponse resp = checkoutManagerService.saveInspectionReport(id, request);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã lưu biên bản", resp));
    }

    @Operation(summary = "Upload ảnh cho 1 đề mục trong biên bản")
    @PostMapping(value = "/{id}/inspection-report/items/{itemKey}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDto<DamageReportResponse>> uploadItemImages(
            @PathVariable Long id,
            @PathVariable String itemKey,
            @RequestPart("images") List<MultipartFile> images
    ) throws IOException {
        DamageReportResponse resp = checkoutManagerService.uploadItemImages(id, itemKey, images);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã tải ảnh", resp));
    }

    @Operation(summary = "Tạo hóa đơn từ biên bản kiểm tra")
    @PostMapping("/{id}/inspection-report/create-invoice")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDto<InvoiceResponse>> createInvoice(
            @PathVariable Long id,
            @RequestParam(name = "dueDate", required = false) LocalDate dueDate
    ) {
        InvoiceResponse resp = checkoutManagerService.createSettlementInvoice(id, dueDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(201, "Đã tạo hóa đơn", resp));
    }
}
