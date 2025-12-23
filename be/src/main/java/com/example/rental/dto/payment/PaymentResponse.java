package com.example.rental.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long invoiceId;
    private String status; // SUCCESS / FAILED / PENDING
    private BigDecimal amount;
    private String method;
    private String providerRef;
    private LocalDateTime timestamp = LocalDateTime.now();
}
