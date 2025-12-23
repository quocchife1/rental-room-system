package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_code", unique = true, nullable = false, length = 20)
    private String reservationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @CreationTimestamp
    @Column(name = "reservation_date")
    private LocalDateTime reservationDate;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_slot", length = 20)
    private VisitTimeSlot visitSlot;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    // --- CÁC TRƯỜNG MỚI ĐƯỢC THÊM ĐỂ KHỚP VỚI REQUEST/MAPPER ---
    @Column(name = "start_date")
    private LocalDateTime startDate; // Ngày bắt đầu thuê dự kiến

    @Column(name = "end_date")
    private LocalDateTime endDate;   // Ngày kết thúc thuê dự kiến

    @Lob
    private String notes;
}