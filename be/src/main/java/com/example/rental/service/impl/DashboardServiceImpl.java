package com.example.rental.service.impl;

import com.example.rental.dto.dashboard.*;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import com.example.rental.service.DashboardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    
    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final BranchRepository branchRepository;
    
    @Override
    public DirectorDashboardDTO getDirectorDashboard(Long branchId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0);
        
        return getDashboardByDateRange(branchId, yearStart, now);
    }
    
    @Override
    public DirectorDashboardDTO getDashboardByDateRange(Long branchId, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        String branchCodeFilter = null;
        if (branchId != null) {
            Branch b = branchRepository.findById(branchId)
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
            branchCodeFilter = b.getBranchCode();
        }

        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<Contract> allContracts = contractRepository.findAll();
        List<Room> allRooms = roomRepository.findAll();
        List<Tenant> allTenants = tenantRepository.findAll();
        List<MaintenanceRequest> allMaintenances = maintenanceRepository.findAll();

        // Branch filter
        if (branchCodeFilter != null && !branchCodeFilter.isBlank()) {
            final String bc = branchCodeFilter.trim();
            allRooms = allRooms.stream()
                    .filter(r -> r != null && r.getBranchCode() != null && r.getBranchCode().equalsIgnoreCase(bc))
                    .collect(Collectors.toList());

            allContracts = allContracts.stream()
                    .filter(c -> c != null && c.getBranchCode() != null && c.getBranchCode().equalsIgnoreCase(bc))
                    .collect(Collectors.toList());

            allInvoices = allInvoices.stream()
                    .filter(inv -> {
                        try {
                            Contract c = inv.getContract();
                            String cBranch = c != null ? c.getBranchCode() : null;
                            return cBranch != null && cBranch.equalsIgnoreCase(bc);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            allMaintenances = allMaintenances.stream()
                    .filter(m -> {
                        try {
                            Room r = m.getRoom();
                            return r != null && r.getBranchCode() != null && r.getBranchCode().equalsIgnoreCase(bc);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Convenience time filters
        List<Invoice> invoicesInRange = allInvoices.stream()
                .filter(inv -> inv.getCreatedAt() != null && !inv.getCreatedAt().isBefore(startDate) && !inv.getCreatedAt().isAfter(endDate))
                .collect(Collectors.toList());

        List<Contract> contractsInRange = allContracts.stream()
                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(startDate) && !c.getCreatedAt().isAfter(endDate))
                .collect(Collectors.toList());

        // === REVENUE ===
        BigDecimal totalRevenueThisMonth = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .filter(inv -> {
                    LocalDateTime t = inv.getCreatedAt();
                    return t != null && !t.isBefore(monthStart) && !t.isAfter(now);
                })
                .map(Invoice::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenueThisYear = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .filter(inv -> {
                    LocalDateTime t = inv.getCreatedAt();
                    return t != null && !t.isBefore(yearStart) && !t.isAfter(now);
                })
                .map(Invoice::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenueAllTime = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly revenue history (last 12 months)
        Map<YearMonth, BigDecimal> revenueByMonth = new HashMap<>();
        for (Invoice inv : allInvoices) {
            if (inv == null || inv.getStatus() != InvoiceStatus.PAID) continue;
            BigDecimal amt = inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount();
            YearMonth ym;
            if (inv.getBillingYear() != null && inv.getBillingMonth() != null) {
                ym = YearMonth.of(inv.getBillingYear(), inv.getBillingMonth());
            } else if (inv.getCreatedAt() != null) {
                ym = YearMonth.from(inv.getCreatedAt());
            } else {
                continue;
            }
            revenueByMonth.merge(ym, amt, BigDecimal::add);
        }
        List<MonthlyRevenueDTO> monthlyRevenueHistory = new ArrayList<>();
        YearMonth startYm = YearMonth.from(now).minusMonths(11);
        for (int i = 0; i < 12; i++) {
            YearMonth ym = startYm.plusMonths(i);
            monthlyRevenueHistory.add(MonthlyRevenueDTO.builder()
                    .month(ym.getMonthValue())
                    .year(ym.getYear())
                    .revenue(revenueByMonth.getOrDefault(ym, BigDecimal.ZERO))
                    .build());
        }

        // Revenue by branch (this month) for pie chart
        List<RevenueByBranchDTO> revenueByBranchThisMonth;
        if (branchCodeFilter != null && !branchCodeFilter.isBlank()) {
            Branch b = branchRepository.findByBranchCode(branchCodeFilter).orElse(null);
            revenueByBranchThisMonth = List.of(RevenueByBranchDTO.builder()
                    .branchId(b != null ? b.getId() : branchId)
                    .branchCode(branchCodeFilter)
                    .branchName(b != null ? b.getBranchName() : branchCodeFilter)
                    .revenue(totalRevenueThisMonth)
                    .build());
        } else {
            List<Branch> branches = branchRepository.findAll();
            Map<String, Branch> branchByCode = branches.stream()
                    .filter(b -> b.getBranchCode() != null && !b.getBranchCode().isBlank())
                    .collect(Collectors.toMap(
                            b -> b.getBranchCode().trim().toLowerCase(Locale.ROOT),
                            b -> b,
                            (a, c) -> a
                    ));

            Map<String, BigDecimal> revenueByBranchCode = allInvoices.stream()
                    .filter(inv -> inv != null && inv.getStatus() == InvoiceStatus.PAID)
                    .filter(inv -> {
                        LocalDateTime t = inv.getCreatedAt();
                        return t != null && !t.isBefore(monthStart) && !t.isAfter(now);
                    })
                    .collect(Collectors.groupingBy(
                            inv -> {
                                try {
                                    Contract c = inv.getContract();
                                    String code = c != null ? c.getBranchCode() : null;
                                    return code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
                                } catch (Exception e) {
                                    return "";
                                }
                            },
                            Collectors.mapping(
                                    inv -> inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount(),
                                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                            )
                    ));

            revenueByBranchThisMonth = revenueByBranchCode.entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue().compareTo(BigDecimal.ZERO) > 0)
                    .map(e -> {
                        String codeKey = e.getKey();
                        Branch b = branchByCode.get(codeKey);
                        String branchCode = (b != null && b.getBranchCode() != null) ? b.getBranchCode() : (codeKey == null ? null : codeKey.toUpperCase(Locale.ROOT));
                        String branchName = b != null ? b.getBranchName() : (branchCode == null || branchCode.isBlank() ? "Không xác định" : branchCode);
                        return RevenueByBranchDTO.builder()
                                .branchId(b != null ? b.getId() : null)
                                .branchCode(branchCode)
                                .branchName(branchName)
                                .revenue(e.getValue())
                                .build();
                    })
                    .sorted(Comparator.comparing(RevenueByBranchDTO::getRevenue, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }

        // === DEBT & OVERDUE ===
        BigDecimal totalOutstandingDebt = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.UNPAID || inv.getStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int overdueInvoiceCount = (int) allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.OVERDUE)
                .count();

        BigDecimal overdueAmount = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OverdueInvoiceDTO> topOverdueInvoices = allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.OVERDUE)
                .map(inv -> {
                    Contract c = inv.getContract();
                    Tenant t = c != null ? c.getTenant() : null;
                    String tenantName = t != null ? t.getFullName() : null;
                    Long daysOverdue = 0L;
                    if (inv.getDueDate() != null) {
                        daysOverdue = Math.max(0, ChronoUnit.DAYS.between(inv.getDueDate(), today));
                    }
                    return OverdueInvoiceDTO.builder()
                            .invoiceId(inv.getId())
                            .contractId(c != null ? c.getId() : null)
                            .tenantName(tenantName)
                            .amount(inv.getAmount())
                            .daysOverdue(daysOverdue)
                            .build();
                })
                .sorted(Comparator.comparing(OverdueInvoiceDTO::getDaysOverdue, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OverdueInvoiceDTO::getAmount, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());

        // === ROOMS & OCCUPANCY ===
        int totalRoomCount = allRooms.size();
        int occupiedRoomCount = (int) allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        int availableRoomCount = (int) allRooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        int maintenanceRoomCount = (int) allRooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count();

        Double occupancyRateThisMonth = totalRoomCount == 0 ? 0.0 : (double) occupiedRoomCount / (double) totalRoomCount;
        Double occupancyRateLastMonth = occupancyRateThisMonth;

        List<RoomOccupancyDTO> roomOccupancyByBranch;
        if (branchCodeFilter != null && !branchCodeFilter.isBlank()) {
            Branch b = branchRepository.findByBranchCode(branchCodeFilter)
                    .orElse(null);
            int total = totalRoomCount;
            int occupied = occupiedRoomCount;
            roomOccupancyByBranch = List.of(RoomOccupancyDTO.builder()
                    .branchId(b != null ? b.getId() : branchId)
                    .branchName(b != null ? b.getBranchName() : branchCodeFilter)
                    .totalRooms(total)
                    .occupiedRooms(occupied)
                    .occupancyRate(total == 0 ? 0.0 : (double) occupied / (double) total)
                    .build());
        } else {
            Map<Long, List<Room>> roomsByBranchId = allRooms.stream()
                    .filter(r -> r.getBranch() != null && r.getBranch().getId() != null)
                    .collect(Collectors.groupingBy(r -> r.getBranch().getId()));
            List<Branch> branches = branchRepository.findAll();
            roomOccupancyByBranch = branches.stream()
                    .map(b -> {
                        List<Room> rooms = roomsByBranchId.getOrDefault(b.getId(), Collections.emptyList());
                        int total = rooms.size();
                        int occupied = (int) rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
                        double rate = total == 0 ? 0.0 : (double) occupied / (double) total;
                        return RoomOccupancyDTO.builder()
                                .branchId(b.getId())
                                .branchName(b.getBranchName())
                                .totalRooms(total)
                                .occupiedRooms(occupied)
                                .occupancyRate(rate)
                                .build();
                    })
                    .sorted(Comparator.comparing(RoomOccupancyDTO::getOccupancyRate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }

        // === CONTRACTS ===
        int activeContractCount = (int) allContracts.stream().filter(c -> c.getStatus() == ContractStatus.ACTIVE).count();
        int newContractsThisMonth = (int) allContracts.stream()
                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(monthStart) && !c.getCreatedAt().isAfter(now))
                .count();

        int contractsEndingThisMonth = (int) allContracts.stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .filter(c -> c.getEndDate() != null)
                .filter(c -> c.getEndDate().getYear() == today.getYear() && c.getEndDate().getMonthValue() == today.getMonthValue())
                .count();

        List<ContractSummaryDTO> expiringContracts = allContracts.stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .filter(c -> c.getEndDate() != null)
                .map(c -> {
                    long days = ChronoUnit.DAYS.between(today, c.getEndDate());
                    Room r = c.getRoom();
                    String roomInfo = r != null ? r.getRoomCode() : (c.getBranchCode() + c.getRoomNumber());
                    Tenant t = c.getTenant();
                    return ContractSummaryDTO.builder()
                            .contractId(c.getId())
                            .tenantName(t != null ? t.getFullName() : null)
                            .roomInfo(roomInfo)
                            .endDate(c.getEndDate().toString())
                            .daysRemaining((int) days)
                            .build();
                })
                .filter(dto -> dto.getDaysRemaining() != null && dto.getDaysRemaining() >= 0 && dto.getDaysRemaining() <= 30)
                .sorted(Comparator.comparing(ContractSummaryDTO::getDaysRemaining))
                .limit(10)
                .collect(Collectors.toList());

        // === TENANTS ===
        int totalTenantCount = allTenants.size();
        int newTenantsThisMonth = (int) allTenants.stream()
                .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(monthStart) && !t.getCreatedAt().isAfter(now))
                .count();

        // === MAINTENANCE ===
        int pendingMaintenanceCount = (int) allMaintenances.stream()
                .filter(m -> m.getStatus() == MaintenanceStatus.PENDING || m.getStatus() == MaintenanceStatus.IN_PROGRESS)
                .count();
        BigDecimal totalMaintenanceCost = allMaintenances.stream()
                .map(MaintenanceRequest::getCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // === PAYMENTS (derived from PAID invoices) ===
        BigDecimal totalPaymentThisMonth = totalRevenueThisMonth;
        int totalPaymentCountThisMonth = (int) allInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID)
                .filter(inv -> {
                    LocalDateTime t = inv.getCreatedAt();
                    return t != null && !t.isBefore(monthStart) && !t.isAfter(now);
                })
                .count();

        return DirectorDashboardDTO.builder()
                .totalRevenueThisMonth(totalRevenueThisMonth)
                .totalRevenueThisYear(totalRevenueThisYear)
                .totalRevenueAllTime(totalRevenueAllTime)
                .monthlyRevenueHistory(monthlyRevenueHistory)
                .revenueByBranchThisMonth(revenueByBranchThisMonth)
                .occupancyRateThisMonth(occupancyRateThisMonth)
                .occupancyRateLastMonth(occupancyRateLastMonth)
                .roomOccupancyByBranch(roomOccupancyByBranch)
                .totalOutstandingDebt(totalOutstandingDebt)
                .overdueInvoiceCount(overdueInvoiceCount)
                .overdueAmount(overdueAmount)
                .topOverdueInvoices(topOverdueInvoices)
                .activeContractCount(activeContractCount)
                .newContractsThisMonth(newContractsThisMonth)
                .contractsEndingThisMonth(contractsEndingThisMonth)
                .expiringContracts(expiringContracts)
                .totalTenantCount(totalTenantCount)
                .newTenantsThisMonth(newTenantsThisMonth)
                .totalRoomCount(totalRoomCount)
                .availableRoomCount(availableRoomCount)
                .occupiedRoomCount(occupiedRoomCount)
                .maintenanceRoomCount(maintenanceRoomCount)
                .pendingMaintenanceCount(pendingMaintenanceCount)
                .totalMaintenanceCost(totalMaintenanceCost)
                .totalPaymentThisMonth(totalPaymentThisMonth)
                .totalPaymentCountThisMonth(totalPaymentCountThisMonth)
                .build();
    }
}
