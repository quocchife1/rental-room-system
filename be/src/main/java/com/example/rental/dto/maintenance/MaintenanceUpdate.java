package com.example.rental.dto.maintenance;

import lombok.Data;
import java.math.BigDecimal;


@Data
public class MaintenanceUpdate {
    private String resolution;
    private BigDecimal cost;
    private String technicianName;
    private String status; // PENDING, IN_PROGRESS, COMPLETED
}
