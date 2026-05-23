package com.example.rental.service.impl;

import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DamageReportServiceImpl Tests")
class DamageReportServiceImplTest {

    @Mock private DamageReportRepository damageReportRepository;
    @Mock private DamageImageRepository damageImageRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private DamageReportServiceImpl damageReportService;

    @BeforeEach
    void setupSecurity() {
        Authentication auth = mock(Authentication.class);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
        when(auth.getName()).thenReturn("staffUser");
        when(employeeRepository.findByUsername("staffUser")).thenReturn(Optional.of(new Employees()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create Damage Report - Success")
    void createDamageReport_Success() throws IOException {
        DamageReportCreateRequest req = new DamageReportCreateRequest();
        req.setContractId(1L);
        req.setTotalDamageCost(new BigDecimal("500000"));

        // Khởi tạo đầy đủ cấu trúc để tránh NPE trong hàm toResponse
        Room room = Room.builder().roomCode("A101").build();
        Contract contract = Contract.builder().id(1L).room(room).build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(damageReportRepository.save(any(DamageReport.class))).thenAnswer(i -> {
            DamageReport dr = i.getArgument(0);
            dr.setId(100L);
            dr.setContract(contract);
            return dr;
        });

        DamageReportResponse response = damageReportService.createDamageReport(req, null);

        assertThat(response.getId()).isEqualTo(100L);
        verify(damageReportRepository).save(any(DamageReport.class));
    }

    @Test
    @DisplayName("Upload Images - Success")
    void uploadDamageImages_Success() throws IOException {
        DamageReport dr = new DamageReport();
        when(damageReportRepository.findById(1L)).thenReturn(Optional.of(dr));
        
        List<MultipartFile> images = List.of(
            new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes())
        );

        String result = damageReportService.uploadDamageImages(1L, images);

        assertThat(result).contains("thành công");
        verify(damageImageRepository).saveAll(any());
    }

    @Test
    @DisplayName("Approve Report - Success")
    void approveDamageReport_Success() {
        // Setup dữ liệu đầy đủ cho entity
        Room room = Room.builder().roomCode("A101").build();
        Contract contract = Contract.builder().id(1L).room(room).build();
        DamageReport dr = DamageReport.builder()
                .status(DamageReportStatus.SUBMITTED)
                .contract(contract)
                .build();
        
        when(damageReportRepository.findById(1L)).thenReturn(Optional.of(dr));
        when(damageReportRepository.save(any(DamageReport.class))).thenReturn(dr);

        DamageReportResponse response = damageReportService.approveDamageReport(1L, "OK");

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        verify(damageReportRepository).save(dr);
    }

    @Test
    @DisplayName("Submit for Approval - Fail if not DRAFT")
    void submitForApproval_FailIfNotDraft() {
        DamageReport dr = DamageReport.builder().status(DamageReportStatus.APPROVED).build();
        when(damageReportRepository.findById(1L)).thenReturn(Optional.of(dr));

        assertThatThrownBy(() -> damageReportService.submitForApproval(1L))
                .isInstanceOf(IllegalStateException.class);
    }
}