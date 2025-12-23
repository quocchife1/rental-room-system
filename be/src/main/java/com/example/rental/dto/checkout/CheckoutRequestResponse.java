package com.example.rental.dto.checkout;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckoutRequestResponse {
    private Long id;
    private Long contractId;
    private Long tenantId;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
}
