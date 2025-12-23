package com.example.rental.entity;

/**
 * Trạng thái phòng: AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE
 */
public enum RoomStatus {
    AVAILABLE,      // Có sẵn
    RESERVED,       // Đã giữ/đặt trước (Nghiệp vụ: sau khi "Đã giữ phòng" [cite: 63])
    OCCUPIED,       // Đang thuê (Nghiệp vụ: sau khi lập hợp đồng [cite: 73])
    MAINTENANCE     // Đang bảo trì
}