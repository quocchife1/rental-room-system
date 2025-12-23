package com.example.rental.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse { // Đã thêm 'public'
    private Long id;
    private String reservationCode;
    private Long tenantId;
    private String tenantName;
    private String tenantPhoneNumber;
    private String tenantEmail;
    private Long roomId;
    private String roomCode;
    private String roomNumber;
    private String status;
    
    // Thông tin ngày tháng
    private LocalDateTime reservationDate; // Ngày tạo phiếu
    private LocalDateTime expirationDate;  // Ngày hết hạn giữ phòng

    // Lịch tham khảo phòng
    private LocalDate visitDate;
    private String visitSlot;

    private LocalDateTime startDate;       // Ngày bắt đầu thuê dự kiến
    private LocalDateTime endDate;         // Ngày kết thúc thuê dự kiến
    
    private String notes;
}