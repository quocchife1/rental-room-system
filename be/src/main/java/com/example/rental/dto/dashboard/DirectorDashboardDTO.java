package com.example.rental.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho Director Dashboard
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectorDashboardDTO {
    
    // === REVENUE ===
    private BigDecimal totalRevenueThisMonth;
    private BigDecimal totalRevenueThisYear;
    private BigDecimal totalRevenueAllTime;
    private List<MonthlyRevenueDTO> monthlyRevenueHistory;
    private List<RevenueByBranchDTO> revenueByBranchThisMonth;
    
    // === OCCUPANCY RATE ===
    private Double occupancyRateThisMonth;
    private Double occupancyRateLastMonth;
    private List<RoomOccupancyDTO> roomOccupancyByBranch;
    
    // === DEBT & OVERDUE ===
    private BigDecimal totalOutstandingDebt;
    private Integer overdueInvoiceCount;
    private BigDecimal overdueAmount;
    private List<OverdueInvoiceDTO> topOverdueInvoices;
    
    // === CONTRACTS ===
    private Integer activeContractCount;
    private Integer newContractsThisMonth;
    private Integer contractsEndingThisMonth;
    private List<ContractSummaryDTO> expiringContracts;
    
    // === TENANTS ===
    private Integer totalTenantCount;
    private Integer newTenantsThisMonth;
    
    // === ROOMS ===
    private Integer totalRoomCount;
    private Integer availableRoomCount;
    private Integer occupiedRoomCount;
    private Integer maintenanceRoomCount;
    
    // === MAINTENANCE ===
    private Integer pendingMaintenanceCount;
    private BigDecimal totalMaintenanceCost;
    
    // === PAYMENTS ===
    private BigDecimal totalPaymentThisMonth;
    private Integer totalPaymentCountThisMonth;
}
