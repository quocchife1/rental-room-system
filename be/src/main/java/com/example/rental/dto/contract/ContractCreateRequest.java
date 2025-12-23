package com.example.rental.dto.contract;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractCreateRequest {
    private String branchCode;     // Nhập mã chi nhánh (VD: CN01)
    private String roomNumber;     // Nhập số phòng (VD: P101)
    private Long tenantId;         // Nếu có sẵn
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
