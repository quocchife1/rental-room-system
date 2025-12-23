package com.example.rental.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long invoiceId;
    private String method; // E.g., CREDIT_CARD, BANK_TRANSFER, QR
    private BigDecimal amount;
    private String providerRef; // provider transaction id (optional)
}
