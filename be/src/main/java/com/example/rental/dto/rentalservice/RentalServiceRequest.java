package com.example.rental.dto.rentalservice;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RentalServiceRequest {
    private String serviceName;
    private BigDecimal price;
    private String unit;
    private String description;
}
