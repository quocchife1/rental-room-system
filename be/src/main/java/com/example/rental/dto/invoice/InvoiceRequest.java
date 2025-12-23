package com.example.rental.dto.invoice;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequest {
    private Long contractId;
    // Optional: when provided, defines the billing period (month/year) independent from dueDate.
    private Integer billingYear;
    private Integer billingMonth;
    private LocalDate dueDate;
    private List<InvoiceDetailRequest> details;
}
