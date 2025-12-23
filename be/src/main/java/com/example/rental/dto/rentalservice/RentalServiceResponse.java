package com.example.rental.dto.rentalservice;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RentalServiceResponse {
    private Long id;
    private String serviceName;
    private BigDecimal price;
    private String unit;
    private String description;
}
