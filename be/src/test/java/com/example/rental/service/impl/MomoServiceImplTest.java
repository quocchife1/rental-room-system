package com.example.rental.service.impl;

import com.example.rental.dto.momo.CreateMomoResponse;
import com.example.rental.dto.momo.CreateMomoRequest;
import com.example.rental.service.MomoClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// THÊM IMPORT NÀY
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy; 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MomoServiceImpl Tests")
class MomoServiceImplTest {

    @Mock private MomoClientService momoClientService;
    @Mock private com.example.rental.repository.PartnerPostRepository partnerPostRepository;
    @Mock private com.example.rental.repository.PartnerPaymentRepository partnerPaymentRepository;
    @Mock private com.example.rental.repository.ContractRepository contractRepository;
    @Mock private com.example.rental.repository.InvoiceRepository invoiceRepository;
    @Mock private com.example.rental.repository.PaymentRepository paymentRepository;
    @Mock private com.example.rental.service.AuditLogService auditLogService;

    @InjectMocks
    private MomoServiceImpl momoService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(momoService, "PARTNER_CODE", "MOMO123");
        ReflectionTestUtils.setField(momoService, "ACCESS_KEY", "accessKey");
        ReflectionTestUtils.setField(momoService, "SECRET_KEY", "secretKey");
        ReflectionTestUtils.setField(momoService, "REQUEST_TYPE", "captureWallet");
    }

    @Test
    @DisplayName("Create ATM Payment - Success")
    void createATMPayment_Success() {
        when(momoClientService.createATMPayment(any(CreateMomoRequest.class)))
            .thenReturn(CreateMomoResponse.builder().payUrl("http://momo.test").build());

        var response = momoService.createATMPayment(100000L, "INV-123", "Info", "http://redirect.com", "extra");

        assertThat(response).isNotNull();
        assertThat(response.getPayUrl()).isEqualTo("http://momo.test");
        verify(momoClientService).createATMPayment(any(CreateMomoRequest.class));
    }

    @Test
    @DisplayName("Create ATM Payment - Locked default method")
    void createATMPayment_DefaultMethod_ThrowsException() {
        assertThatThrownBy(() -> momoService.createATMPayment(100000L, "INV-123"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
