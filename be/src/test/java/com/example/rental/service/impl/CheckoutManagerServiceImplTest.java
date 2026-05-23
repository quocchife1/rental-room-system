package com.example.rental.service.impl;

import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import com.example.rental.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CheckoutManagerServiceImpl Tests")
class CheckoutManagerServiceImplTest {

    @Mock private CheckoutRequestRepository checkoutRequestRepository;
    @Mock private DamageReportRepository damageReportRepository;
    @Mock private DamageImageRepository damageImageRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceDetailRepository invoiceDetailRepository;
    @Mock private EmailService emailService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private CheckoutManagerServiceImpl checkoutManagerService;

    @BeforeEach
    void setupSecurity() {
        Authentication auth = mock(Authentication.class);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        // DÙNG DO-RETURN ĐỂ ÉP KIỂU, TRÁNH LỖI GENERIC WILDCARD
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))).when(auth).getAuthorities();
        when(auth.getName()).thenReturn("managerUser");
        
        Branch branch = Branch.builder().branchCode("B01").build();
        Employees emp = Employees.builder().branch(branch).build();
        when(employeeRepository.findByUsername("managerUser")).thenReturn(Optional.of(emp));
    }

    @Test
    @DisplayName("Save Inspection Report - Success")
    void saveInspectionReport_ShouldUpdateDetails() {
        CheckoutRequest req = CheckoutRequest.builder()
                .contract(Contract.builder().branchCode("B01").build())
                .build();
        DamageReport dr = DamageReport.builder().status(DamageReportStatus.DRAFT).build();
        
        when(checkoutRequestRepository.findById(1L)).thenReturn(Optional.of(req));
        when(damageReportRepository.findByCheckoutRequestId(1L)).thenReturn(Optional.of(dr));
        when(damageReportRepository.save(any())).thenReturn(dr);

        DamageReportCreateRequest request = new DamageReportCreateRequest();
        request.setDescription("Updated desc");
        request.setTotalDamageCost(new BigDecimal("100000"));

        var response = checkoutManagerService.saveInspectionReport(1L, request);

        assertThat(response.getDescription()).isEqualTo("Updated desc");
    }
}