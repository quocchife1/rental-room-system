package com.example.rental.dto.booking;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCleaningBookingRequest {
    // Optional; if null will book next Thursday
    private LocalDate bookingDate;
}
