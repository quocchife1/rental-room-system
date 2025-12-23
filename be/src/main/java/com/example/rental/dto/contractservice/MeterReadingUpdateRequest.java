package com.example.rental.dto.contractservice;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeterReadingUpdateRequest {
    private BigDecimal previousReading;
    private BigDecimal currentReading;
}
