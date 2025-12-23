package com.example.rental.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private Long contractId;
    private Long tenantId;
    private String tenantUsername;
    private String tenantFullName;
    private String tenantEmail;
    private String tenantPhoneNumber;
    private String tenantCccd;
    private String tenantStudentId;
    private String tenantUniversity;
    private Long branchId;
    private String branchCode;
    private String branchName;
    private Long roomId;
    private String roomCode;
    private String roomNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Integer billingYear;
    private Integer billingMonth;
    private LocalDate paidDate;
    private Boolean paidDirect;
    private String paymentReference;
    private String status;
    private LocalDateTime createdAt;
    private List<InvoiceDetailResponse> details;
}
