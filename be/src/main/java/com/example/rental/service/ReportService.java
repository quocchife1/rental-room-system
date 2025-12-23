package com.example.rental.service;

import com.example.rental.dto.reports.FinancialReportSummaryDTO;

import java.time.LocalDate;

public interface ReportService {
    FinancialReportSummaryDTO getSummary(LocalDate from, LocalDate to, Long branchId);
}
