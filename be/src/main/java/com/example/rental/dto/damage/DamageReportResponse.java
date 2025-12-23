package com.example.rental.dto.damage;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response cho báo cáo hư hỏng
 */
@Data
public class DamageReportResponse {

    private Long id;
    private Long checkoutRequestId;
    private Long contractId;
    private String contractCode;
    private String tenantName;
    private String roomCode;
    private String inspectorName;
    private String description;
    private String damageDetails;
    private BigDecimal totalDamageCost;
    private Long settlementInvoiceId;
    private String status; // DRAFT, SUBMITTED, APPROVED, REJECTED
    private String approverName;
    private String approverNote;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private List<DamageImageDto> images;
}
