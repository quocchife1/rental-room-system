package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.reports.FinancialReportSummaryDTO;
import com.example.rental.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Financial reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','ACCOUNTANT')")
    @Operation(summary = "Financial summary report")
    public ResponseEntity<ApiResponseDto<FinancialReportSummaryDTO>> summary(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) Long branchId
    ) {
        FinancialReportSummaryDTO dto = reportService.getSummary(from, to, branchId);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Report fetched", dto));
    }
}
