package com.example.rental.repository;

import com.example.rental.entity.DamageReport;
import com.example.rental.entity.DamageReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DamageReportRepository extends JpaRepository<DamageReport, Long> {
    
    @Query("SELECT dr FROM DamageReport dr WHERE dr.contract.id = :contractId")
    List<DamageReport> findByContractId(@Param("contractId") Long contractId);

    @Query("SELECT dr FROM DamageReport dr WHERE dr.checkoutRequest.id = :requestId")
    java.util.Optional<DamageReport> findByCheckoutRequestId(@Param("requestId") Long requestId);

    @Query("SELECT dr FROM DamageReport dr WHERE dr.settlementInvoiceId = :invoiceId")
    java.util.Optional<DamageReport> findBySettlementInvoiceId(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT dr FROM DamageReport dr WHERE dr.status = :status ORDER BY dr.createdAt DESC")
    Page<DamageReport> findByStatus(@Param("status") DamageReportStatus status, Pageable pageable);
    
    @Query("SELECT dr FROM DamageReport dr WHERE dr.createdAt BETWEEN :startDate AND :endDate ORDER BY dr.createdAt DESC")
    List<DamageReport> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
