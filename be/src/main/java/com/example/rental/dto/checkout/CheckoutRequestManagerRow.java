package com.example.rental.dto.checkout;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckoutRequestManagerRow {
    private Long id;
    private String status;
    private String reason;
    private LocalDateTime createdAt;

    private Long contractId;
    private String roomCode;
    private String roomNumber;
    private String branchCode;

    private Long tenantId;
    private String tenantName;
    private String tenantPhoneNumber;
}
