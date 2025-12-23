package com.example.rental.entity;

/**
 * Trạng thái hóa đơn: UNPAID, PAID, OVERDUE
 */
public enum InvoiceStatus {
    UNPAID,     // Chưa thanh toán
    PAID,       // Đã thanh toán
    OVERDUE     // Quá hạn
}