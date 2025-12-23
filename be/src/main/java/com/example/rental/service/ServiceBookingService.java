package com.example.rental.service;

import com.example.rental.dto.booking.CreateCleaningBookingRequest;
import com.example.rental.dto.booking.CleaningBookingManagerRow;
import com.example.rental.dto.booking.ServiceBookingResponse;

import java.util.List;

public interface ServiceBookingService {
    ServiceBookingResponse createNextCleaningBooking(Long contractId, CreateCleaningBookingRequest request);

    List<ServiceBookingResponse> listBookingsForContract(Long contractId);

    ServiceBookingResponse markCompleted(Long bookingId);

    List<CleaningBookingManagerRow> listCleaningBookingsMyBranch();

    ServiceBookingResponse cancelBookingAsManager(Long bookingId, String reason);
}
