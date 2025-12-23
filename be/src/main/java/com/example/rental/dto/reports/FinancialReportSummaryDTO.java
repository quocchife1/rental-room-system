package com.example.rental.dto.reports;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FinancialReportSummaryDTO {
    private BigDecimal revenue;
    private BigDecimal paid;
    private BigDecimal outstanding;
    private Integer invoiceCount;
}
