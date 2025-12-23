package com.example.rental.controller;

import com.example.rental.dto.dashboard.DirectorDashboardDTO;
import com.example.rental.dto.ApiResponseDto;
import org.springframework.data.domain.Page;
import com.example.rental.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "API dashboard cho Giám đốc/Admin")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/director")
        @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'MANAGER')")
    @Operation(summary = "Lấy dashboard tổng quát cho Giám đốc")
    public ResponseEntity<ApiResponseDto<DirectorDashboardDTO>> getDirectorDashboard(
            @RequestParam(required = false) Long branchId) {
            DirectorDashboardDTO dto = dashboardService.getDirectorDashboard(branchId);
            return ResponseEntity.ok(ApiResponseDto.success(200, "Director dashboard fetched", dto));
    }
    
    @GetMapping("/director/date-range")
        @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'MANAGER')")
    @Operation(summary = "Lấy dashboard với phạm vi ngày tùy chỉnh")
    public ResponseEntity<ApiResponseDto<DirectorDashboardDTO>> getDashboardByDateRange(
            @RequestParam(required = false) Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
            DirectorDashboardDTO dto = dashboardService.getDashboardByDateRange(branchId, startDate, endDate);
            return ResponseEntity.ok(ApiResponseDto.success(200, "Director dashboard fetched", dto));
    }
}
