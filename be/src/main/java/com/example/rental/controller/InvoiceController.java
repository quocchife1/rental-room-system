package com.example.rental.controller;

import com.example.rental.dto.invoice.InvoiceRequest;
import com.example.rental.dto.invoice.InvoiceResponse;
import com.example.rental.dto.ApiResponseDto;
import org.springframework.data.domain.Page;
import com.example.rental.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice API", description = "Quản lý hóa đơn")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final com.example.rental.service.TenantService tenantService;

    @Operation(summary = "Tạo hóa đơn mới")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    public ResponseEntity<ApiResponseDto<InvoiceResponse>> create(@RequestBody InvoiceRequest request) {
        InvoiceResponse resp = invoiceService.create(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponseDto.success(201, "Invoice created", resp));
    }

    @Operation(summary = "Tạo hóa đơn hàng tháng cho tất cả hợp đồng ACTIVE")
    @PostMapping("/generate-monthly")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<com.example.rental.dto.invoice.MonthlyInvoiceGenerateResponse>> generateMonthly(
            @RequestBody com.example.rental.dto.invoice.MonthlyInvoiceGenerateRequest request
    ) {
        var resp = invoiceService.generateMonthlyInvoices(request);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Monthly invoices generated", resp));
    }

    @Operation(summary = "Xem trước chi tiết hóa đơn tháng cho toàn bộ hợp đồng ACTIVE")
    @GetMapping("/monthly-previews")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<java.util.List<com.example.rental.dto.invoice.ContractMonthlyInvoicePreviewResponse>>> monthlyPreviews(
            @RequestParam int year,
            @RequestParam int month
    ) {
        var rows = invoiceService.previewMonthlyInvoices(year, month);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Monthly invoice previews fetched", rows));
    }

    @Operation(summary = "Tạo hóa đơn tháng cho 1 hợp đồng")
    @PostMapping("/generate-monthly/contracts/{contractId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<InvoiceResponse>> generateMonthlyForContract(
            @PathVariable Long contractId,
            @RequestBody com.example.rental.dto.invoice.MonthlyInvoiceGenerateRequest request
    ) {
        var resp = invoiceService.generateMonthlyInvoiceForContract(contractId, request);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Monthly invoice generated", resp));
    }

    @Operation(summary = "Lấy hóa đơn theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT','RECEPTIONIST') or hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<InvoiceResponse>> getById(@PathVariable Long id) {
        InvoiceResponse resp = invoiceService.getById(id);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Invoice fetched", resp));
    }

    @Operation(summary = "Danh sách hóa đơn")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<java.util.List<InvoiceResponse>>> getAll() {
        java.util.List<InvoiceResponse> list = invoiceService.getAll();
        return ResponseEntity.ok(ApiResponseDto.success(200, "Invoices fetched", list));
    }

    @Operation(summary = "Danh sách hóa đơn (phân trang)")
    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<Page<InvoiceResponse>>> getAllPaged(
            org.springframework.data.domain.Pageable pageable,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) com.example.rental.entity.InvoiceStatus status
    ) {
        Page<InvoiceResponse> page = invoiceService.search(pageable, year, month, status);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Invoices page fetched", page));
    }

    @Operation(summary = "Thanh toán hóa đơn (trực tiếp hoặc online)")
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','ACCOUNTANT') or hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<InvoiceResponse>> payInvoice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean direct
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAccountant = auth != null && auth.getAuthorities() != null
            && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ACCOUNTANT".equals(a.getAuthority()));
        if (isAccountant && !direct) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.error(HttpStatus.FORBIDDEN.value(), "Kế toán chỉ được xác nhận thu tiền mặt", null));
        }
        InvoiceResponse resp = invoiceService.markPaid(id, direct);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Invoice paid", resp));
    }

    @Operation(summary = "Lấy hóa đơn của người dùng đang đăng nhập (Tenant)")
    @GetMapping("/my-invoices")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<java.util.List<InvoiceResponse>>> getMyInvoices() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var tenantOpt = tenantService.findByUsername(username);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.success(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Không tìm thấy người thuê", null));
        }
        var tenant = tenantOpt.get();
        java.util.List<InvoiceResponse> list = invoiceService.getInvoicesForTenant(tenant.getId());
        return ResponseEntity.ok(ApiResponseDto.success(200, "Invoices fetched for tenant", list));
    }

    @Operation(summary = "Lấy hóa đơn của người dùng đang đăng nhập (Tenant) - phân trang")
    @GetMapping("/my-invoices/paged")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<org.springframework.data.domain.Page<InvoiceResponse>>> getMyInvoicesPaged(org.springframework.data.domain.Pageable pageable) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var tenantOpt = tenantService.findByUsername(username);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.success(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Không tìm thấy người thuê", null));
        }
        var tenant = tenantOpt.get();
        var page = invoiceService.getInvoicesForTenant(tenant.getId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Invoices page fetched for tenant", page));
    }

    @Operation(summary = "Gửi nhắc thanh toán cho 1 hóa đơn")
    @PostMapping("/{id}/send-reminder")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<Void>> sendReminder(@PathVariable Long id) {
        invoiceService.sendReminderForInvoice(id);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Reminder sent"));
    }
    
}


