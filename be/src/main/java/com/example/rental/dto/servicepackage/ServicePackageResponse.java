package com.example.rental.dto.servicepackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer durationDays;
    private String description;
}
