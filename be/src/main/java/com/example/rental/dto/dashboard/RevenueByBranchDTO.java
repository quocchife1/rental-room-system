package com.example.rental.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueByBranchDTO {
    private Long branchId;
    private String branchCode;
    private String branchName;
    private BigDecimal revenue;
}
