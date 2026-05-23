package com.example.rental.service.impl;

import com.example.rental.dto.payment.PaymentRequest;
import com.example.rental.dto.payment.PaymentResponse;
import com.example.rental.entity.Payment;
import com.example.rental.repository.PaymentRepository;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.InvoiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    @Mock private InvoiceService invoiceService;
    @Mock private AuditLogService auditLogService;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Process Payment - Success")
    void processPayment_Success() {
        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(1L);
        request.setAmount(new BigDecimal("100000"));
        request.setMethod("CASH");

        // Giả lập lưu Payment thành công
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(99L);
            return p;
        });

        PaymentResponse response = paymentService.processPayment(request);

        // Verify luồng chính
        assertThat(response.getInvoiceId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getPaymentId()).isEqualTo(99L);
        
        verify(invoiceService).markPaid(1L, true);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogService).logAction(anyString(), anyString(), any(), anyString(), anyLong(), anyString(), any(), anyString(), anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Process Payment - Still returns success even if Audit fails")
    void processPayment_ReturnsSuccessEvenIfAuditFails() {
        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(1L);
        
        // Giả lập AuditLog gây lỗi
        doThrow(new RuntimeException("Audit service down")).when(auditLogService)
            .logAction(anyString(), anyString(), any(), anyString(), anyLong(), anyString(), any(), anyString(), anyString(), any(), anyString(), anyString(), any());

        // Vẫn phải gọi thành công
        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        verify(invoiceService).markPaid(1L, true);
    }

    @Test
    @DisplayName("Initiate MoMo - Success")
    void initiateMoMo_Success() {
        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(1L);
        request.setAmount(new BigDecimal("200000"));

        String redirectUrl = paymentService.initiateMoMo(request, "/billing");

        assertThat(redirectUrl).contains("fake-redirect");
        assertThat(redirectUrl).contains("invoiceId=1");
        verify(paymentRepository).save(any(Payment.class));
    }
}
