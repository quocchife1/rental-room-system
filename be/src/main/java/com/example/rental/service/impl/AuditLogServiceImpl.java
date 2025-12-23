package com.example.rental.service.impl;

import com.example.rental.dto.audit.AuditLogDTO;
import com.example.rental.dto.audit.AuditLogSearchCriteria;
import com.example.rental.entity.*;
import com.example.rental.mapper.AuditLogMapper;
import com.example.rental.repository.AuditLogRepository;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.AuditStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(String actorId, String actorRole, AuditAction action, 
                              String targetType, Long targetId, String description, 
                              String oldValue, String newValue, String ipAddress, 
                              Long branchId, String userAgent,
                              String status, String errorMessage) {
        // Truncate large payloads to avoid DB/storage issues
        int MAX_LEN = 2000;
        String safeOld = oldValue != null ? (oldValue.length() > MAX_LEN ? oldValue.substring(0, MAX_LEN) : oldValue) : null;
        String safeNew = newValue != null ? (newValue.length() > MAX_LEN ? newValue.substring(0, MAX_LEN) : newValue) : null;

        AuditLog auditLog = AuditLog.builder()
                .actorId(actorId)
                .actorRole(actorRole)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .oldValue(safeOld)
                .newValue(safeNew)
                .ipAddress(ipAddress)
                .branchId(branchId)
                .userAgent(userAgent)
                .status(status != null ? status : "SUCCESS")
                .errorMessage(errorMessage)
                .build();

        // Try saving with a simple retry to improve reliability
        int attempts = 0;
        while (attempts < 2) {
            try {
                AuditLog saved = auditLogRepository.save(auditLog);
                log.debug("Audit saved: id={} actor={} action={} status={}", saved.getId(), saved.getActorId(), saved.getAction(), saved.getStatus());
                return saved;
            } catch (Exception ex) {
                attempts++;
                log.warn("[Audit] Attempt {}/2 failed to save audit log: {}", attempts, ex.getMessage());
                if (attempts >= 2) {
                    log.error("[Audit] Failed to save audit log after {} attempts: {}", attempts, ex.getMessage(), ex);
                    return null;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditTrail(String targetType, Long targetId) {
        List<AuditLog> auditLogs = auditLogRepository.findByTargetTypeAndId(targetType, targetId);
        return auditLogs.stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditTrailPaged(String targetType, Long targetId, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByTargetTypeAndIdPaged(targetType, targetId, pageable);
        return auditLogs.map(auditLogMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getByAction(AuditAction action, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByAction(action, pageable);
        return auditLogs.map(auditLogMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getByActorId(String actorId, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByActorId(actorId, pageable);
        return auditLogs.map(auditLogMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByDateRange(startDate, endDate, pageable);
        return auditLogs.map(auditLogMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getByBranch(Long branchId, Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByBranchId(branchId, pageable);
        return auditLogs.map(auditLogMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getPaymentHistory(Long invoiceId) {
        List<AuditLog> auditLogs = auditLogRepository.findByTargetTypeAndId("INVOICE", invoiceId);
        return auditLogs.stream()
                .filter(log -> log.getAction() == AuditAction.CONFIRM_PAYMENT)
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getConfirmedPayments(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> auditLogs = auditLogRepository.findConfirmedPayments(startDate, endDate);
        return auditLogs.stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getContractAuditTrail(Long contractId) {
        List<AuditLog> auditLogs = auditLogRepository.findContractChanges(contractId);
        return auditLogs.stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuditLogDTO getById(Long auditLogId) {
        AuditLog auditLog = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new RuntimeException("Audit log not found"));
        return auditLogMapper.toDTO(auditLog);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuditStatistics getStatistics(LocalDateTime startDate, LocalDateTime endDate, Long branchId) {
        Page<AuditLog> auditLogsPage = auditLogRepository.findByBranchId(branchId, Pageable.unpaged());
        List<AuditLog> auditLogs = auditLogsPage.getContent().stream()
                .filter(log -> log.getCreatedAt().isAfter(startDate) && log.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());
        
        return AuditStatistics.builder()
                .totalActions((long) auditLogs.size())
                .totalActors(auditLogs.stream().map(AuditLog::getActorId).distinct().count())
                .totalModifiedEntities(auditLogs.stream().map(AuditLog::getTargetType).distinct().count())
                .createCount(auditLogs.stream().filter(log -> log.getAction() == AuditAction.CREATE_CONTRACT).count())
                .updateCount(auditLogs.stream().filter(log -> log.getAction() == AuditAction.UPDATE_CONTRACT).count())
                .deleteCount(auditLogs.stream().filter(log -> log.getAction() == AuditAction.DELETE_DATA).count())
                .approveCount(auditLogs.stream().filter(log -> log.getAction() == AuditAction.APPROVE_PARTNER_POST).count())
                .rejectCount(auditLogs.stream().filter(log -> log.getAction() == AuditAction.REJECT_PARTNER_POST).count())
                .confirmPaymentCount(auditLogs.stream().filter(log -> log.getAction() == AuditAction.CONFIRM_PAYMENT).count())
                .contractChanges(auditLogs.stream().filter(log -> log.getTargetType().equals("CONTRACT")).count())
                .invoiceChanges(auditLogs.stream().filter(log -> log.getTargetType().equals("INVOICE")).count())
                .tenantChanges(auditLogs.stream().filter(log -> log.getTargetType().equals("TENANT")).count())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAll(org.springframework.data.domain.Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        return page.map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> search(AuditLogSearchCriteria criteria, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            LocalDateTime from = parseDateTime(criteria.getFrom());
            LocalDateTime to = parseDateTime(criteria.getTo());
            if (from != null && to != null) {
                predicates.add(cb.between(root.get("createdAt"), from, to));
            } else if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            } else if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            if (criteria.getBranchId() != null) {
                predicates.add(cb.equal(root.get("branchId"), criteria.getBranchId()));
            }

            if (criteria.getActor() != null && !criteria.getActor().isBlank()) {
                String needle = "%" + criteria.getActor().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("actorId")), needle));
            }

            if (criteria.getAction() != null && !criteria.getAction().isBlank()) {
                predicates.add(cb.equal(root.get("action"), AuditAction.valueOf(criteria.getAction())));
            }

            if (criteria.getEntityType() != null && !criteria.getEntityType().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("targetType")), criteria.getEntityType().toUpperCase()));
            }

            if (criteria.getEntityId() != null) {
                predicates.add(cb.equal(root.get("targetId"), criteria.getEntityId()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toDTO);
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        // Support both datetime-local (yyyy-MM-ddTHH:mm) and full ISO_DATE_TIME
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignore) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException ignore2) {
                return null;
            }
        }
    }
}
