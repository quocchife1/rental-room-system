package com.example.rental.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverdueInvoiceDTO {
    private Long invoiceId;
    private Long contractId;
    private String tenantName;
    private BigDecimal amount;
    private Long daysOverdue;
}
