package com.example.rental.dto.audit;

import lombok.Data;

@Data
public class AuditLogSearchCriteria {
    private String from;
    private String to;
    private String actor;
    private String action;
    private String entityType;
    private Long entityId;
    private Long branchId;
}
