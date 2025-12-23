package com.example.rental.dto.contractservice;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ContractServiceResponse {
    private Long id;
    private String serviceName;
    private Integer quantity;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private LocalDate startDate;
    private LocalDate endDate;
}
