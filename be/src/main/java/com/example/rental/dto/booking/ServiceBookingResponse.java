package com.example.rental.dto.booking;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ServiceBookingResponse {
    private Long id;
    private Long contractId;
    private Long serviceId;
    private String serviceName;
    private LocalDate bookingDate;
    private String startTime;
    private String endTime;
    private String status;
    private LocalDateTime createdAt;
}
