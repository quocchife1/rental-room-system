package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.employee.EmployeeResponse;
import com.example.rental.entity.UserStatus;
import com.example.rental.mapper.EmployeeMapper;
import com.example.rental.service.EmployeeService;
import com.example.rental.exception.ResourceNotFoundException; // Sử dụng Exception đã tạo
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    /**
     * Lấy danh sách tất cả nhân viên
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> responses = employeeService.findAllEmployees().stream()
                .map(employeeMapper::toResponse)
                .toList();
                
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Danh sách nhân viên", 
                responses)
        );
    }

        @GetMapping("/paged")
        public ResponseEntity<ApiResponseDto<org.springframework.data.domain.Page<EmployeeResponse>>> getEmployeesPaged(org.springframework.data.domain.Pageable pageable) {
                org.springframework.data.domain.Page<EmployeeResponse> page = employeeService.findAllEmployees(pageable);
                return ResponseEntity.ok(ApiResponseDto.success(200, "Employees page fetched", page));
        }

    /**
     * Lấy thông tin chi tiết của một nhân viên theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.findById(id)
                .map(employeeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Chi tiết nhân viên", 
                response)
        );
    }

    /**
     * Cập nhật trạng thái (Kích hoạt/Khóa) của nhân viên
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDto<EmployeeResponse>> updateEmployeeStatus(
            @PathVariable Long id, 
            @RequestParam UserStatus status) 
    {
        // Giả định logic chỉ cho phép ACTIVE/BANNED, các trạng thái khác sẽ được xử lý trong service
        if (status != UserStatus.ACTIVE && status != UserStatus.BANNED) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận ACTIVE hoặc BANNED.");
        }
        
        // Cập nhật và chuyển đổi sang DTO
        EmployeeResponse response = employeeMapper.toResponse(employeeService.updateStatus(id, status));

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), 
                "Cập nhật trạng thái nhân viên thành công", 
                response)
        );
    }
    
    // NOTE: Các API cho phép cập nhật hồ sơ cá nhân nên dùng DTO riêng (ví dụ: EmployeeUpdateRequest) và nên được xử lý qua PATCH/PUT.
}