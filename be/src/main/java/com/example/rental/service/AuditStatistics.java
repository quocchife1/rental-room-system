package com.example.rental.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditStatistics {
    private Long totalActions;
    private Long totalActors;
    private Long totalModifiedEntities;
    
    // Action statistics
    private Long createCount;
    private Long updateCount;
    private Long deleteCount;
    private Long approveCount;
    private Long rejectCount;
    
    // Financial statistics
    private Long confirmPaymentCount;
    private BigDecimal totalPaymentAmount;
    
    // User statistics
    private Long adminActions;
    private Long managerActions;
    private Long accountantActions;
    
    // Entity statistics
    private Long contractChanges;
    private Long invoiceChanges;
    private Long tenantChanges;
}
