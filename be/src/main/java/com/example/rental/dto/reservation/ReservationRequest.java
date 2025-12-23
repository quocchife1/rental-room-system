package com.example.rental.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import com.example.rental.entity.VisitTimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    
    @NotNull(message = "Vui lòng chọn phòng")
    private Long roomId;

    @NotNull(message = "Vui lòng chọn ngày đến tham khảo")
    private LocalDate visitDate;

    @NotNull(message = "Vui lòng chọn khung giờ đến")
    private VisitTimeSlot visitSlot;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String notes;
}