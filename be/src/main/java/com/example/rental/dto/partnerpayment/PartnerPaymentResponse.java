package com.example.rental.dto.partnerpayment;

import com.example.rental.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerPaymentResponse {
    private Long id;
    private String paymentCode;
    private Long partnerId;
    private String partnerName;
    private Long postId;
    private String postTitle;
    private BigDecimal amount;
    private PaymentMethod method;
    private LocalDateTime paidDate;
}
