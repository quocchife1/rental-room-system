package com.example.rental.service.impl;

import com.example.rental.dto.reports.FinancialReportSummaryDTO;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReportServiceImpl Tests")
class ReportServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private MaintenanceRequestRepository maintenanceRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        // Mock nhân viên cho mọi test
        Branch branch = Branch.builder().id(1L).branchCode("B01").build();
        Employees staff = Employees.builder().branch(branch).build(); // Gán branch vào đây
        when(employeeRepository.findByUsername(anyString())).thenReturn(Optional.of(staff));
    }

    @Test
    @DisplayName("Get Summary - Success (Admin View)")
    void getSummary_SuccessAdmin() {
        Invoice inv1 = Invoice.builder().amount(new BigDecimal("1000000")).status(InvoiceStatus.PAID).build();
        Invoice inv2 = Invoice.builder().amount(new BigDecimal("500000")).status(InvoiceStatus.UNPAID).build();
        
        when(invoiceRepository.findForReport(any(), any(), any())).thenReturn(List.of(inv1, inv2));
        when(invoiceRepository.findPaidForReport(any(), any(), any())).thenReturn(List.of(inv1));

        FinancialReportSummaryDTO summary = reportService.getSummary(LocalDate.now(), LocalDate.now(), null);

        assertThat(summary.getRevenue()).isEqualByComparingTo("1500000");
    }

    @Test
    @DisplayName("Get Summary - Manager Scope Filtering")
    void getSummary_ManagerFiltering() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("managerUser", null, List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
        );

        Employees emp = Employees.builder().branch(Branch.builder().id(99L).build()).build();
        when(employeeRepository.findByUsername("managerUser")).thenReturn(Optional.of(emp));
        
        when(invoiceRepository.findForReport(any(), any(), anyLong())).thenReturn(Collections.emptyList());
        when(invoiceRepository.findPaidForReport(any(), any(), anyLong())).thenReturn(Collections.emptyList());

        reportService.getSummary(LocalDate.now(), LocalDate.now(), 1L);

        verify(invoiceRepository).findForReport(any(), any(), eq(99L));
    }
}