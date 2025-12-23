package com.example.rental.dto.invoice;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceDetailRequest {
    private String description;
    private BigDecimal unitPrice;   // giá 1 đơn vị
    private Integer quantity = 1;   // số lượng
}
