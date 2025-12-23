package com.example.rental.dto.contractservice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractServiceRequest {
    private Long serviceId;
    private Integer quantity;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private LocalDate startDate;
    private LocalDate endDate;
}
