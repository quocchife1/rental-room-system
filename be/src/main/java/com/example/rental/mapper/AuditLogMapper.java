package com.example.rental.mapper;

import com.example.rental.dto.audit.AuditLogDTO;
import com.example.rental.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    
    AuditLogDTO toDTO(AuditLog auditLog);
    
    AuditLog toEntity(AuditLogDTO auditLogDTO);
}
