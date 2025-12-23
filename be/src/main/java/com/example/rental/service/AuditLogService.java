package com.example.rental.service;

import com.example.rental.entity.AuditLog;
import com.example.rental.entity.AuditAction;
import com.example.rental.dto.audit.AuditLogDTO;
import com.example.rental.dto.audit.AuditLogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    
    /**
     * Ghi nhận một hành động (được gọi tự động bởi AuditAspect)
     */
    AuditLog logAction(String actorId, String actorRole, AuditAction action, 
                       String targetType, Long targetId, String description, 
                       String oldValue, String newValue, String ipAddress, 
                       Long branchId, String userAgent,
                       String status, String errorMessage);
    
    /**
     * Tìm tất cả audit log của một entity
     */
    List<AuditLogDTO> getAuditTrail(String targetType, Long targetId);
    
    /**
     * Phân trang audit log của một entity
     */
    Page<AuditLogDTO> getAuditTrailPaged(String targetType, Long targetId, Pageable pageable);
    
    /**
     * Tìm audit log theo action
     */
    Page<AuditLogDTO> getByAction(AuditAction action, Pageable pageable);
    
    /**
     * Tìm audit log của một actor
     */
    Page<AuditLogDTO> getByActorId(String actorId, Pageable pageable);
    
    /**
     * Tìm audit log trong khoảng thời gian
     */
    Page<AuditLogDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Tìm audit log của một branch
     */
    Page<AuditLogDTO> getByBranch(Long branchId, Pageable pageable);

    /**
     * Lấy tất cả audit log theo trang
     */
    Page<AuditLogDTO> getAll(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Tìm lịch sử thanh toán
     */
    List<AuditLogDTO> getPaymentHistory(Long invoiceId);
    
    /**
     * Tìm tất cả thanh toán được xác nhận trong một khoảng thời gian
     */
    List<AuditLogDTO> getConfirmedPayments(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Tìm audit log của hợp đồng
     */
    List<AuditLogDTO> getContractAuditTrail(Long contractId);
    
    /**
     * Lấy chi tiết audit log
     */
    AuditLogDTO getById(Long auditLogId);
    
    /**
     * Tính toán thống kê audit
     */
    AuditStatistics getStatistics(LocalDateTime startDate, LocalDateTime endDate, Long branchId);

    /**
     * Advanced search with multiple optional filters
     */
    Page<AuditLogDTO> search(AuditLogSearchCriteria criteria, Pageable pageable);
}
