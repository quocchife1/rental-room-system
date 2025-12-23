package com.example.rental.dto.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InvoiceDetailResponse {
    private Long id;
    private String description;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal amount; // tổng = unitPrice * quantity
}
