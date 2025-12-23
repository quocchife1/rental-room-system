package com.example.rental.entity;

/**
 * Trạng thái duyệt tin: PENDING_APPROVAL, APPROVED, REJECTED
 */
public enum PostApprovalStatus {
    PENDING_PAYMENT, // Chờ thanh toán
    PENDING_APPROVAL, // Chờ duyệt [cite: 26]
    APPROVED,         // Đã duyệt [cite: 27]
    REJECTED          // Bị từ chối
}