package com.example.rental.repository;

import com.example.rental.entity.Reservation;
import com.example.rental.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Tìm danh sách theo Tenant ID
    List<Reservation> findByTenantId(Long tenantId);
    Page<Reservation> findByTenantId(Long tenantId, Pageable pageable);

    // Tìm danh sách theo Room ID
    List<Reservation> findByRoomId(Long roomId);

    void deleteByRoom_IdIn(java.util.List<java.lang.Long> roomIds);

    // Tìm danh sách theo Trạng thái
    List<Reservation> findByStatus(ReservationStatus status);
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    // Receptionist view: only reservations for a specific branch (by room.branchCode)
    Page<Reservation> findByRoomBranchCode(String branchCode, Pageable pageable);
    Page<Reservation> findByRoomBranchCodeAndStatus(String branchCode, ReservationStatus status, Pageable pageable);

    @Query("""
        select r from Reservation r
        join r.tenant t
        join r.room rm
        where
            lower(r.reservationCode) like lower(concat('%', :q, '%'))
            or lower(t.fullName) like lower(concat('%', :q, '%'))
            or lower(t.username) like lower(concat('%', :q, '%'))
            or lower(t.email) like lower(concat('%', :q, '%'))
            or lower(t.phoneNumber) like lower(concat('%', :q, '%'))
            or lower(rm.roomNumber) like lower(concat('%', :q, '%'))
            or lower(rm.roomCode) like lower(concat('%', :q, '%'))
        order by r.reservationDate desc
    """)
    Page<Reservation> search(@Param("q") String q, Pageable pageable);

    @Query("""
        select r from Reservation r
        join r.tenant t
        join r.room rm
        where
            rm.branchCode = :branchCode
            and (
                lower(r.reservationCode) like lower(concat('%', :q, '%'))
                or lower(t.fullName) like lower(concat('%', :q, '%'))
                or lower(t.username) like lower(concat('%', :q, '%'))
                or lower(t.email) like lower(concat('%', :q, '%'))
                or lower(t.phoneNumber) like lower(concat('%', :q, '%'))
                or lower(rm.roomNumber) like lower(concat('%', :q, '%'))
                or lower(rm.roomCode) like lower(concat('%', :q, '%'))
            )
            and (:status is null or r.status = :status)
        order by r.reservationDate desc
    """)
    Page<Reservation> searchInBranch(
            @Param("branchCode") String branchCode,
            @Param("q") String q,
            @Param("status") ReservationStatus status,
            Pageable pageable
    );

    @Query("""
        select r from Reservation r
        join r.tenant t
        join r.room rm
        where
            (
                lower(r.reservationCode) like lower(concat('%', :q, '%'))
                or lower(t.fullName) like lower(concat('%', :q, '%'))
                or lower(t.username) like lower(concat('%', :q, '%'))
                or lower(t.email) like lower(concat('%', :q, '%'))
                or lower(t.phoneNumber) like lower(concat('%', :q, '%'))
                or lower(rm.roomNumber) like lower(concat('%', :q, '%'))
                or lower(rm.roomCode) like lower(concat('%', :q, '%'))
            )
            and (:status is null or r.status = :status)
        order by r.reservationDate desc
    """)
    Page<Reservation> searchGlobal(
            @Param("q") String q,
            @Param("status") ReservationStatus status,
            Pageable pageable
    );
}