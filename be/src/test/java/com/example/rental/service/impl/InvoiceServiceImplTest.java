package com.example.rental.service.impl;

import com.example.rental.dto.invoice.*;
import com.example.rental.entity.*;
import com.example.rental.mapper.InvoiceMapper;
import com.example.rental.repository.*;
import com.example.rental.service.EmailService;
import com.example.rental.utils.InvoiceEmailTemplateUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
//import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceServiceImpl Tests")
class InvoiceServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceDetailRepository invoiceDetailRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private EmailService emailService;
    @Mock private ContractServiceRepository contractServiceRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    @DisplayName("Create Invoice - Success")
    void create_Success() {
        // 1. Setup Tenant (Tránh lỗi NullPointerException khi lấy email)
        Tenant tenant = Tenant.builder()
                .email("tenant@test.com")
                .fullName("Test Tenant")
                .build();

        // 2. Setup Contract và Room
        Room room = Room.builder()
                .price(new BigDecimal("5000000"))
                .build();
        
        Contract contract = Contract.builder()
                .id(1L)
                .room(room)
                .tenant(tenant)
                .services(Collections.emptyList())
                .build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        
        // Mock save để trả về entity được lưu
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        InvoiceRequest request = new InvoiceRequest();
        request.setContractId(1L);
        request.setBillingYear(2026);
        request.setBillingMonth(5);

        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class);
             MockedStatic<InvoiceEmailTemplateUtil> util = mockStatic(InvoiceEmailTemplateUtil.class)) {
            
            mapper.when(() -> InvoiceMapper.toResponse(any())).thenReturn(InvoiceResponse.builder().id(1L).build());
            util.when(() -> InvoiceEmailTemplateUtil.buildNewInvoiceEmail(any(), any())).thenReturn("<html>Email</html>");
            
            InvoiceResponse response = invoiceService.create(request);

            // Kiểm tra kết quả
            assertThat(response).isNotNull();
            verify(invoiceRepository, times(2)).save(any(Invoice.class));
            verify(invoiceDetailRepository).saveAll(anyList());
            verify(emailService).sendHtmlMessage(eq("tenant@test.com"), anyString(), anyString());
        }
    }
}