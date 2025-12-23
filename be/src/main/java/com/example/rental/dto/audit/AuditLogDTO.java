package com.example.rental.dto.audit;

import com.example.rental.entity.AuditAction;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDTO {
    private Long id;
    
    // Actor information
    private String actorId;
    private String actorRole;
    
    // Action
    private AuditAction action;
    
    // Target information
    private String targetType;
    private Long targetId;
    
    // Changes
    private String description;
    private String oldValue;
    private String newValue;
    
    // Metadata
    private Long branchId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    
    // Status
    private String status; // SUCCESS, FAILURE
    private String errorMessage;
}
