package com.example.rental.dto.maintenance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaintenanceResponse {
    private Long id;
    private String requestCode;
    private String tenantName;
    private String branchCode;
    private String branchName;
    private String roomNumber;
    private String description;
    private String status;
    private String resolution;
    private BigDecimal cost;
    private String technicianName;
    private Long invoiceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images;
}
