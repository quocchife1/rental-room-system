package com.example.rental.dto.employee;

import com.example.rental.entity.EmployeePosition;
import com.example.rental.entity.UserStatus;
import com.example.rental.dto.branch.BranchResponse; // Sử dụng BranchResponse
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private EmployeePosition position;
    private LocalDate hireDate;
    private UserStatus status;

    // Chi nhánh mà nhân viên trực thuộc
    private BranchResponse branch;
}