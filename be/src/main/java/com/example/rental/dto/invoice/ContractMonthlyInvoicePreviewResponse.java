package com.example.rental.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ContractMonthlyInvoicePreviewResponse {
    private Long contractId;
    private String branchCode;
    private String roomNumber;
    private String tenantName;
    private String tenantUsername;

    private Integer billingYear;
    private Integer billingMonth;
    private LocalDate dueDate;

    private BigDecimal amount;
    private List<InvoiceDetailResponse> details;

    // If invoice preview cannot be computed (e.g., missing meter readings), return error but keep other rows.
    private String error;
}
