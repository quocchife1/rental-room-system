package com.example.rental.dto.contract;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractUpdateRequest {
    // cập nhật thông tin hợp đồng khi còn PENDING
    private String tenantFullName;
    private String tenantPhoneNumber;
    private String tenantEmail;
    private String tenantAddress;
    private String tenantCccd;
    private String studentId;
    private String university;

    private BigDecimal deposit;
    private LocalDate startDate;
    private LocalDate endDate;
}
