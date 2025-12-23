package com.example.rental.dto.booking;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CleaningBookingManagerRow {
    private Long id;
    private Long contractId;
    private String contractCode;
    private String roomCode;
    private String roomNumber;
    private String tenantName;
    private String tenantUsername;
    private LocalDate bookingDate;
    private String startTime;
    private String endTime;
    private String status;
    private String cancelReason;
    private LocalDateTime createdAt;
}
