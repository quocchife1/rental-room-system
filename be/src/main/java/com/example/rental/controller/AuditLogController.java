package com.example.rental.controller;

import com.example.rental.dto.audit.AuditLogDTO;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.AuditStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "API quản lý nhật ký kiểm toán (chỉ Giám đốc/Admin)")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    @GetMapping("/{targetType}/{targetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    @Operation(summary = "Lấy lịch sử thay đổi của một entity")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<AuditLogDTO>>> getAuditTrail(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        java.util.List<AuditLogDTO> auditLogs = auditLogService.getAuditTrail(targetType, targetId);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit trail fetched", auditLogs));
    }
    
    @GetMapping("/{targetType}/{targetId}/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    @Operation(summary = "Lấy lịch sử thay đổi (phân trang)")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<AuditLogDTO>>> getAuditTrailPaged(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            Pageable pageable) {
        org.springframework.data.domain.Page<AuditLogDTO> auditLogs = auditLogService.getAuditTrailPaged(targetType, targetId, pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit page fetched", auditLogs));
    }
    
    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy audit log theo hành động")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<AuditLogDTO>>> getByAction(
            @PathVariable String action,
            Pageable pageable) {
        org.springframework.data.domain.Page<AuditLogDTO> auditLogs = auditLogService.getByAction(
            com.example.rental.entity.AuditAction.valueOf(action), pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit by action fetched", auditLogs));
    }
    
    @GetMapping("/actor/{actorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy tất cả hành động của một actor")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<AuditLogDTO>>> getByActorId(
            @PathVariable String actorId,
            Pageable pageable) {
        org.springframework.data.domain.Page<AuditLogDTO> auditLogs = auditLogService.getByActorId(actorId, pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit by actor fetched", auditLogs));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy audit log trong khoảng thời gian")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<AuditLogDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        org.springframework.data.domain.Page<AuditLogDTO> auditLogs = auditLogService.getByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit by date range fetched", auditLogs));
    }
    
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy audit log của một chi nhánh")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<AuditLogDTO>>> getByBranch(
            @PathVariable Long branchId,
            Pageable pageable) {
        org.springframework.data.domain.Page<AuditLogDTO> auditLogs = auditLogService.getByBranch(branchId, pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit by branch fetched", auditLogs));
    }
    
    @GetMapping("/payment-history/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    @Operation(summary = "Lấy lịch sử thanh toán của hóa đơn")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<AuditLogDTO>>> getPaymentHistory(
            @PathVariable Long invoiceId) {
        java.util.List<AuditLogDTO> auditLogs = auditLogService.getPaymentHistory(invoiceId);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Payment history fetched", auditLogs));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy audit log (phân trang)")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<AuditLogDTO>>> getAllPaged(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<AuditLogDTO> page = auditLogService.getAll(pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit logs page fetched", page));
    }
    
    @GetMapping("/confirmed-payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    @Operation(summary = "Lấy tất cả thanh toán được xác nhận trong khoảng thời gian")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<AuditLogDTO>>> getConfirmedPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        java.util.List<AuditLogDTO> auditLogs = auditLogService.getConfirmedPayments(startDate, endDate);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Confirmed payments fetched", auditLogs));
    }
    
    @GetMapping("/contract/{contractId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy lịch sử thay đổi của hợp đồng")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<AuditLogDTO>>> getContractAuditTrail(
            @PathVariable Long contractId) {
        java.util.List<AuditLogDTO> auditLogs = auditLogService.getContractAuditTrail(contractId);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Contract audit trail fetched", auditLogs));
    }
    
    @GetMapping("/{auditLogId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy chi tiết audit log")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<AuditLogDTO>> getById(@PathVariable Long auditLogId) {
        AuditLogDTO auditLog = auditLogService.getById(auditLogId);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit log fetched", auditLog));
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy thống kê audit log")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<AuditStatistics>> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam Long branchId) {
        AuditStatistics statistics = auditLogService.getStatistics(startDate, endDate, branchId);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit statistics fetched", statistics));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    @Operation(summary = "Tìm audit log nâng cao (lọc nhiều điều kiện)")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<Page<AuditLogDTO>>> search(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) Long branchId,
            Pageable pageable
    ) {
        com.example.rental.dto.audit.AuditLogSearchCriteria criteria = new com.example.rental.dto.audit.AuditLogSearchCriteria();
        criteria.setFrom(from);
        criteria.setTo(to);
        criteria.setActor(actor);
        criteria.setAction(action);
        criteria.setEntityType(entityType);
        criteria.setEntityId(entityId);
        criteria.setBranchId(branchId);
        Page<AuditLogDTO> page = auditLogService.search(criteria, pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Audit search fetched", page));
    }
}
