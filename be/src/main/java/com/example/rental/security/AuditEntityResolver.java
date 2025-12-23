package com.example.rental.security;

import com.example.rental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolve "old/new" snapshots for a subset of audited target types.
 * This keeps AuditAspect generic without hard-coding entity internals.
 */
@Component
@RequiredArgsConstructor
public class AuditEntityResolver {

    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantRepository tenantRepository;
    private final PartnerPostRepository partnerPostRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final ReservationRepository reservationRepository;

    public Object resolve(String targetType, Long targetId) {
        if (targetType == null || targetId == null) return null;

        return switch (targetType) {
            case "ROOM" -> roomRepository.findById(targetId).orElse(null);
            case "CONTRACT" -> contractRepository.findById(targetId).orElse(null);
            case "INVOICE" -> invoiceRepository.findById(targetId).orElse(null);
            case "TENANT" -> tenantRepository.findById(targetId).orElse(null);
            case "RESERVATION" -> reservationRepository.findById(targetId).orElse(null);
            case "PARTNER_POST" -> partnerPostRepository.findById(targetId).orElse(null);
            case "MAINTENANCE_REQUEST" -> maintenanceRequestRepository.findById(targetId).orElse(null);
            case "SYSTEM_CONFIG" -> systemConfigRepository.findById(targetId).orElse(null);
            default -> null;
        };
    }
}
