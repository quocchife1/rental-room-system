package com.example.rental.service.impl;

import com.example.rental.entity.PartnerPayment;
import com.example.rental.repository.PartnerPaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PartnerPaymentServiceImpl Tests")
class PartnerPaymentServiceImplTest {

    @Mock
    private PartnerPaymentRepository partnerPaymentRepository;

    @InjectMocks
    private PartnerPaymentServiceImpl partnerPaymentService;

    @Test
    @DisplayName("Record Payment - Success")
    void recordPayment_Success() {
        PartnerPayment payment = new PartnerPayment();
        payment.setAmount(new java.math.BigDecimal("500000"));
        
        when(partnerPaymentRepository.save(any(PartnerPayment.class))).thenReturn(payment);

        PartnerPayment saved = partnerPaymentService.recordPayment(payment);

        assertThat(saved).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("500000");
        verify(partnerPaymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Find By Id - Success")
    void findById_Success() {
        PartnerPayment payment = new PartnerPayment();
        when(partnerPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Optional<PartnerPayment> result = partnerPaymentService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(payment);
        verify(partnerPaymentRepository).findById(1L);
    }

    @Test
    @DisplayName("Find Payments By Partner Id - Success")
    void findPaymentsByPartnerId_Success() {
        PartnerPayment payment = new PartnerPayment();
        when(partnerPaymentRepository.findByPartnerId(10L)).thenReturn(List.of(payment));

        List<PartnerPayment> results = partnerPaymentService.findPaymentsByPartnerId(10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(payment);
        verify(partnerPaymentRepository).findByPartnerId(10L);
    }
}
