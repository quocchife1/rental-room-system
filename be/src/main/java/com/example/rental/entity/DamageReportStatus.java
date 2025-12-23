package com.example.rental.entity;

/**
 * Trạng thái báo cáo hư hỏng
 */
public enum DamageReportStatus {
    DRAFT,      // Soạn thảo
    SUBMITTED,  // Đã gửi
    APPROVED,   // Được phê duyệt
    REJECTED    // Bị từ chối
}
