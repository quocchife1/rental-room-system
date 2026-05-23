package com.example.rental.service.impl;

import com.example.rental.dto.reservation.ReservationRequest;
import com.example.rental.dto.reservation.ReservationResponse;
import com.example.rental.entity.*;
import com.example.rental.mapper.ReservationMapper;
import com.example.rental.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl Tests")
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ReservationMapper reservationMapper;
    @Mock private GuestRepository guestRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create Reservation - Success")
    void createReservation_Success() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    
        // ÉP CỨNG NGÀY ĐỂ VƯỢT QUA validateVisitDate
        LocalDate targetDate = LocalDate.of(2026, 5, 25); 
        ReservationRequest request = new ReservationRequest();
        request.setRoomId(1L);
        request.setVisitDate(targetDate);
    
        Room room = Room.builder().id(1L).status(RoomStatus.AVAILABLE).build();
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(tenantRepository.findByUsername(anyString())).thenReturn(Optional.of(new Tenant()));
        when(reservationMapper.toEntity(any())).thenReturn(new Reservation());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(reservationMapper.toResponse(any())).thenReturn(ReservationResponse.builder().build());
    
        ReservationResponse response = reservationService.createReservation(request);
    
        assertThat(response).isNotNull();
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    @DisplayName("Create Reservation - Fail if Room Unavailable")
    void createReservation_RoomUnavailable() {
        ReservationRequest request = new ReservationRequest();
        request.setRoomId(1L);
        
        Room room = Room.builder().status(RoomStatus.OCCUPIED).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .hasMessageContaining("Phòng này hiện không có sẵn");
    }

    @Test
    @DisplayName("Confirm Reservation - Success")
    void confirmReservation_Success() {
        Reservation r = Reservation.builder().room(new Room()).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenReturn(r);
        when(reservationMapper.toResponse(any())).thenReturn(ReservationResponse.builder().build());

        reservationService.confirmReservation(1L);

        assertThat(r.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(r.getRoom().getStatus()).isEqualTo(RoomStatus.RESERVED);
    }
}