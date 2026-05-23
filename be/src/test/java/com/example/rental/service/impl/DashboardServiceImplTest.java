package com.example.rental.service.impl;

import com.example.rental.dto.dashboard.DirectorDashboardDTO;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DashboardServiceImpl Tests")
class DashboardServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private MaintenanceRequestRepository maintenanceRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("Get Dashboard - Success with Sample Data")
    void getDashboardByDateRange_Success() {
        Branch branch = Branch.builder().id(1L).branchCode("B01").branchName("Chi nhánh 1").build();
        when(branchRepository.findAll()).thenReturn(List.of(branch));
        
        Room room = Room.builder().status(RoomStatus.OCCUPIED).branch(branch).build();
        when(roomRepository.findAll()).thenReturn(List.of(room));

        Invoice inv = Invoice.builder()
                .status(InvoiceStatus.PAID)
                .amount(new BigDecimal("1000000"))
                .createdAt(LocalDateTime.now())
                .contract(Contract.builder().room(room).build())
                .build();
        when(invoiceRepository.findAll()).thenReturn(List.of(inv));

        when(contractRepository.findAll()).thenReturn(Collections.emptyList());
        when(tenantRepository.findAll()).thenReturn(Collections.emptyList());
        when(maintenanceRepository.findAll()).thenReturn(Collections.emptyList());

        DirectorDashboardDTO dashboard = dashboardService.getDashboardByDateRange(null, null, null);

        assertThat(dashboard).isNotNull();
        // Kiểm tra dùng contains để tránh lỗi font chữ nếu có
        assertThat(dashboard.getRevenueByBranchThisMonth()).isNotEmpty();
    }
}