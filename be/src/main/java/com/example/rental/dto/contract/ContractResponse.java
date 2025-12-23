package com.example.rental.dto.contract;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractResponse {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String tenantPhoneNumber;
    private String tenantEmail;
    private String tenantAddress;
    private String tenantCccd;
    private String studentId;
    private String university;
    private String roomCode;
    private String roomNumber;
    private String branchCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal deposit;
    private String status;
    private LocalDateTime createdAt;
    private String contractFileUrl;
    private String signedContractUrl;

    // Deposit payment gate (after signed upload)
    private String depositPaymentMethod;
    private LocalDateTime depositPaidDate;
    private String depositPaymentReference;
    private String depositInvoiceUrl;
    private String depositReceiptUrl;
}
