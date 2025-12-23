package com.example.rental.dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractPrefillResponse {
    private Long reservationId;

    private String branchCode;
    private String roomNumber;

    private Long tenantId;
    private String tenantFullName;
    private String tenantPhoneNumber;
    private String tenantEmail;
    private String tenantAddress;
    private String tenantCccd;
    private String studentId;
    private String university;

    private BigDecimal deposit;
    private LocalDate startDate;
}
