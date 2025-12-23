package com.example.rental.service;

import com.example.rental.dto.dashboard.DirectorDashboardDTO;
import java.time.LocalDateTime;

public interface DashboardService {
    
    /**
     * Lấy dashboard cho Giám đốc/Admin
     */
    DirectorDashboardDTO getDirectorDashboard(Long branchId);
    
    /**
     * Lấy doanh thu chi tiết theo khoảng thời gian
     */
    DirectorDashboardDTO getDashboardByDateRange(Long branchId, LocalDateTime startDate, LocalDateTime endDate);
}
