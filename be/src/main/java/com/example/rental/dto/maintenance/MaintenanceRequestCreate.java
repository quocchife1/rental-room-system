package com.example.rental.dto.maintenance;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MaintenanceRequestCreate {
    private String tenantName;
    private Long tenantId; // optional: prefer tenantId from authenticated user
    private String branchCode;
    private String roomNumber;
    private String description;
    private MultipartFile[] images;
}
