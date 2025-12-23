package com.example.rental.dto.invoice;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MonthlyInvoiceGenerateRequest {
    private Integer year;
    private Integer month;
    // Optional; if null defaults to last day of month
    private LocalDate dueDate;
}
