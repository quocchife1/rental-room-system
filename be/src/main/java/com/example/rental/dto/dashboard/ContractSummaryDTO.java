package com.example.rental.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractSummaryDTO {
    private Long contractId;
    private String tenantName;
    private String roomInfo;
    private String endDate;
    private Integer daysRemaining;
}
