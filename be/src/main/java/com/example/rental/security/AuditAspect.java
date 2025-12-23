package com.example.rental.security;

import com.example.rental.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    
    private final AuditLogService auditLogService;
    private final AuditEntityResolver auditEntityResolver;
    private final ObjectMapper objectMapper;
    
    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = null;

        // Resolve targetId early so we can capture "oldValue" for update-like actions
        Long targetId = extractTargetIdFromArgs(joinPoint.getArgs());
        String oldValueJson = null;
        if (targetId != null) {
            Object oldEntity = auditEntityResolver.resolve(audited.targetType(), targetId);
            oldValueJson = toJsonSafe(oldEntity);
        }
        
        try {
            result = joinPoint.proceed();
            
            // Ghi nhận hành động thành công
            logAuditAction(audited, targetId, oldValueJson, result, null, "SUCCESS");
            
            return result;
        } catch (Exception e) {
            // Ghi nhận lỗi
            logAuditAction(audited, targetId, oldValueJson, null, e.getMessage(), "FAILURE");
            
            throw e;
        }
    }
    
    private void logAuditAction(Audited audited, Long targetIdFromArgs, String oldValueJson, Object result, String errorMessage, String status) {
        try {
            // Lấy thông tin người dùng hiện tại (bảo vệ null)
            String actorId = "ANONYMOUS";
            String actorRole = "ANONYMOUS";
            try {
                if (SecurityContextHolder.getContext() != null &&
                        SecurityContextHolder.getContext().getAuthentication() != null) {
                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    if (principal instanceof CustomUserDetails) {
                        CustomUserDetails userDetails = (CustomUserDetails) principal;
                        actorId = userDetails.getUsername();
                        actorRole = userDetails.getUserRole();
                    } else if (principal instanceof String) {
                        actorId = (String) principal;
                        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities() != null &&
                                !SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty()) {
                            actorRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
                        }
                    } else if (SecurityContextHolder.getContext().getAuthentication().getName() != null) {
                        actorId = SecurityContextHolder.getContext().getAuthentication().getName();
                    }
                }
            } catch (Exception ex) {
                log.debug("Could not resolve authentication principal for audit", ex);
            }
            
            // Lấy IP address
            String ipAddress = "127.0.0.1";
            String userAgent = "UNKNOWN";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ipAddress = getClientIpAddress(request);
                    userAgent = request.getHeader("User-Agent");
                }
            } catch (Exception e) {
                log.debug("Could not get request context for audit", e);
            }
            
            // Determine action (map to *_FAILED if needed)
            com.example.rental.entity.AuditAction actionToLog = audited.action();
            if ("FAILURE".equals(status)) {
                try {
                    actionToLog = com.example.rental.entity.AuditAction.valueOf(audited.action().name() + "_FAILED");
                } catch (IllegalArgumentException ex) {
                    // If no specific FAILED action exists, keep original
                    actionToLog = audited.action();
                }
            }

            // Prefer targetId from args; otherwise try from result
            Long targetId = targetIdFromArgs != null ? targetIdFromArgs : extractTargetId(result);

            // If we still don't have oldValue but we have targetId, try resolving now
            String safeOld = oldValueJson;
            if (safeOld == null && targetId != null) {
                safeOld = toJsonSafe(auditEntityResolver.resolve(audited.targetType(), targetId));
            }

            // For newValue, try resolver after write (fresh state) when possible
            String safeNew = null;
            if (targetId != null) {
                Object newEntity = auditEntityResolver.resolve(audited.targetType(), targetId);
                safeNew = toJsonSafe(newEntity);
            }
            if (safeNew == null) {
                safeNew = toJsonSafe(result);
            }

            Long branchId = extractBranchId(result);

                log.debug("AuditAspect: preparing to save audit - actorId={}, actorRole={}, action={}, targetType={}, targetId={}, branchId={}, status={}",
                    actorId, actorRole, actionToLog, audited.targetType(), targetId, branchId, status);

            auditLogService.logAction(
                    actorId,
                    actorRole,
                    actionToLog,
                    audited.targetType(),
                    targetId,
                    audited.description(),
                    safeOld,
                    safeNew,
                    ipAddress,
                    branchId,
                    userAgent,
                    status,
                    errorMessage
            );
            
            log.info("✓ Audit logged: {} - {} by {}", audited.action(), audited.targetType(), actorId);
        } catch (Exception e) {
            log.error("Error logging audit action", e);
        }
    }

    private Long extractTargetIdFromArgs(Object[] args) {
        if (args == null || args.length == 0) return null;
        for (Object arg : args) {
            if (arg == null) continue;
            if (arg instanceof Long) return (Long) arg;
            if (arg instanceof Integer) return ((Integer) arg).longValue();

            // try common getId()
            try {
                java.lang.reflect.Method m = arg.getClass().getMethod("getId");
                Object idVal = m.invoke(arg);
                if (idVal instanceof Long) return (Long) idVal;
                if (idVal instanceof Integer) return ((Integer) idVal).longValue();
            } catch (Exception ignored) {
            }
        }
        return null;
    }
    
    private Long extractTargetId(Object result) {
        if (result == null) return null;
        
        try {
            if (result instanceof Long) return (Long) result;
            if (result instanceof Integer) return ((Integer) result).longValue();
            
            // Nếu là object với trường id
            if (result.getClass().getDeclaredField("id") != null) {
                java.lang.reflect.Field idField = result.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object idValue = idField.get(result);
                if (idValue instanceof Long) return (Long) idValue;
                if (idValue instanceof Integer) return ((Integer) idValue).longValue();
            }
        } catch (Exception e) {
            log.debug("Could not extract target ID from result", e);
        }
        
        return null;
    }

    private Long extractBranchId(Object result) {
        if (result == null) return null;
        try {
            // try getBranchId method
            try {
                java.lang.reflect.Method m = result.getClass().getMethod("getBranchId");
                Object val = m.invoke(result);
                if (val instanceof Long) return (Long) val;
                if (val instanceof Integer) return ((Integer) val).longValue();
            } catch (NoSuchMethodException ignored) {}

            // try getBranch().getId()
            try {
                java.lang.reflect.Method m2 = result.getClass().getMethod("getBranch");
                Object branch = m2.invoke(result);
                if (branch != null) {
                    java.lang.reflect.Field idField = branch.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object idVal = idField.get(branch);
                    if (idVal instanceof Long) return (Long) idVal;
                    if (idVal instanceof Integer) return ((Integer) idVal).longValue();
                }
            } catch (NoSuchMethodException ignored) {}
        } catch (Exception e) {
            log.debug("Could not extract branch id", e);
        }
        return null;
    }

    private String toJsonSafe(Object obj) {
        if (obj == null) return null;
        try {
            // If it's a primitive wrapper or string, serialize directly
            if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
                return objectMapper.writeValueAsString(obj);
            }

            // If it's a collection or map, don't deeply serialize entities inside; return size info
            if (obj instanceof java.util.Collection) {
                java.util.Map<String, Object> summary = new java.util.HashMap<>();
                summary.put("class", obj.getClass().getSimpleName());
                summary.put("size", ((java.util.Collection<?>) obj).size());
                return objectMapper.writeValueAsString(summary);
            }
            if (obj instanceof java.util.Map) {
                java.util.Map<String, Object> summary = new java.util.HashMap<>();
                summary.put("class", obj.getClass().getSimpleName());
                summary.put("size", ((java.util.Map<?, ?>) obj).size());
                return objectMapper.writeValueAsString(summary);
            }

            // For JPA entities or complex objects, build a lightweight summary to avoid cycles
            java.util.Map<String, Object> summary = new java.util.HashMap<>();
            summary.put("class", obj.getClass().getSimpleName());

            // try common id getters
            try {
                java.lang.reflect.Method m = obj.getClass().getMethod("getId");
                Object id = m.invoke(obj);
                summary.put("id", id);
            } catch (Exception ignore) {
            }

            // try to pick a few helpful fields without deep traversal
            String[] getters = new String[] {"getBranchCode", "getRoomNumber", "getUsername", "getEmail", "getEmployeeCode", "getRoomCode"};
            for (String g : getters) {
                try {
                    java.lang.reflect.Method gm = obj.getClass().getMethod(g);
                    Object v = gm.invoke(obj);
                    if (v != null) summary.put(g.replace("get", "").toLowerCase(), v);
                } catch (Exception ignored) {
                }
            }

            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            try {
                return obj.toString();
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
