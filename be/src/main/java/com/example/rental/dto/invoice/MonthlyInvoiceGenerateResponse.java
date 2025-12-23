package com.example.rental.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonthlyInvoiceGenerateResponse {
    private int totalActiveContracts;
    private int createdCount;
    private int skippedExistingCount;
    private List<Long> createdInvoiceIds;
}
