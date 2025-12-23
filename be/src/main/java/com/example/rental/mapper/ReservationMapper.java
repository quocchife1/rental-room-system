package com.example.rental.mapper;

import com.example.rental.dto.reservation.ReservationRequest;
import com.example.rental.dto.reservation.ReservationResponse;
import com.example.rental.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    
    public Reservation toEntity(ReservationRequest request) {
        return Reservation.builder()
                .visitDate(request.getVisitDate())
                .visitSlot(request.getVisitSlot())
                .startDate(request.getStartDate()) // Map start date
                .endDate(request.getEndDate())     // Map end date
                .notes(request.getNotes())
                .build();
    }
    
    public ReservationResponse toResponse(Reservation reservation) {
        if (reservation == null) return null;

        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .tenantId(reservation.getTenant() != null ? reservation.getTenant().getId() : null)
                .tenantName(reservation.getTenant() != null ? reservation.getTenant().getFullName() : null)
            .tenantPhoneNumber(reservation.getTenant() != null ? reservation.getTenant().getPhoneNumber() : null)
            .tenantEmail(reservation.getTenant() != null ? reservation.getTenant().getEmail() : null)
                .roomId(reservation.getRoom() != null ? reservation.getRoom().getId() : null)
                .roomCode(reservation.getRoom() != null ? reservation.getRoom().getRoomCode() : null)
                .roomNumber(reservation.getRoom() != null ? reservation.getRoom().getRoomNumber() : null)
                .status(reservation.getStatus() != null ? reservation.getStatus().toString() : null)
                .reservationDate(reservation.getReservationDate())
                .expirationDate(reservation.getExpirationDate())
                .visitDate(reservation.getVisitDate())
                .visitSlot(reservation.getVisitSlot() != null ? reservation.getVisitSlot().toString() : null)
                .startDate(reservation.getStartDate()) // Map start date
                .endDate(reservation.getEndDate())     // Map end date
                .notes(reservation.getNotes())
                .build();
    }
}