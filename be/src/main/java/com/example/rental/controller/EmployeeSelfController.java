package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.employee.EmployeeResponse;
import com.example.rental.mapper.EmployeeMapper;
import com.example.rental.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Self API", description = "Thông tin nhân viên đang đăng nhập")
public class EmployeeSelfController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT','RECEPTIONIST','MAINTENANCE','SECURITY')")
    @Operation(summary = "Lấy hồ sơ nhân viên đang đăng nhập")
    public ResponseEntity<ApiResponseDto<EmployeeResponse>> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var employee = employeeService.findByUsername(username)
                .orElseThrow(() -> new com.example.rental.exception.ResourceNotFoundException("Employee", "username", username));
        EmployeeResponse response = employeeMapper.toResponse(employee);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Employee profile fetched", response));
    }
}