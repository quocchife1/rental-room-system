package com.example.rental.entity;

/**
 * Trạng thái yêu cầu bảo trì: PENDING, IN_PROGRESS, COMPLETED
 */
public enum MaintenanceStatus {
    PENDING,     // Đang chờ xử lý
    IN_PROGRESS, // Đang tiến hành
    COMPLETED    // Hoàn thành
}