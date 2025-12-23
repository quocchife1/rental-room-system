package com.example.rental.repository;

import com.example.rental.entity.AuditLog;
import com.example.rental.entity.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    
    /**
     * Tìm tất cả audit log của một entity cụ thể
     */
    @Query("SELECT a FROM AuditLog a WHERE a.targetType = :targetType AND a.targetId = :targetId ORDER BY a.createdAt DESC")
    List<AuditLog> findByTargetTypeAndId(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    /**
     * Phân trang audit log của một entity
     */
    @Query("SELECT a FROM AuditLog a WHERE a.targetType = :targetType AND a.targetId = :targetId ORDER BY a.createdAt DESC")
    Page<AuditLog> findByTargetTypeAndIdPaged(@Param("targetType") String targetType, @Param("targetId") Long targetId, Pageable pageable);
    
    /**
     * Tìm audit log theo action
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.createdAt DESC")
    Page<AuditLog> findByAction(@Param("action") AuditAction action, Pageable pageable);
    
    /**
     * Tìm audit log theo actor
     */
    @Query("SELECT a FROM AuditLog a WHERE a.actorId = :actorId ORDER BY a.createdAt DESC")
    Page<AuditLog> findByActorId(@Param("actorId") String actorId, Pageable pageable);
    
    /**
     * Tìm audit log trong khoảng thời gian
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    /**
     * Tìm audit log theo branch
     */
    @Query("SELECT a FROM AuditLog a WHERE a.branchId = :branchId ORDER BY a.createdAt DESC")
    Page<AuditLog> findByBranchId(@Param("branchId") Long branchId, Pageable pageable);
    
    /**
     * Tìm tất cả thay đổi của hợp đồng (Contract)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.targetType = 'CONTRACT' AND a.targetId = :contractId ORDER BY a.createdAt DESC")
    List<AuditLog> findContractChanges(@Param("contractId") Long contractId);
    
    /**
     * Tìm tất cả thanh toán đã xác nhận
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'CONFIRM_PAYMENT' AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findConfirmedPayments(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
