package com.example.rental.repository;

import com.example.rental.entity.ServiceBooking;
import com.example.rental.entity.ServiceBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {
    List<ServiceBooking> findByContract_Id(Long contractId);

    boolean existsByContract_IdAndService_IdAndBookingDate(Long contractId, Long serviceId, LocalDate bookingDate);

    List<ServiceBooking> findByContract_IdAndStatusAndBookingDateBetween(Long contractId, ServiceBookingStatus status, LocalDate from, LocalDate to);

    List<ServiceBooking> findByContract_IdAndStatusInAndBookingDateBetween(Long contractId, List<ServiceBookingStatus> statuses, LocalDate from, LocalDate to);

        @org.springframework.data.jpa.repository.Query("""
                select b from ServiceBooking b
                    join fetch b.contract c
                    join fetch c.tenant t
                    join fetch c.room r
                    join fetch b.service s
                 where r.branchCode = :branchCode
                     and lower(s.serviceName) like lower(concat('%', :serviceNameLike, '%'))
                     and (:fromDate is null or b.bookingDate >= :fromDate)
                     and (:status is null or b.status = :status)
                 order by b.bookingDate asc, b.startTime asc
                """)
        List<ServiceBooking> findForBranchAndServiceNameFromDate(
                        @org.springframework.data.repository.query.Param("branchCode") String branchCode,
                        @org.springframework.data.repository.query.Param("serviceNameLike") String serviceNameLike,
                        @org.springframework.data.repository.query.Param("fromDate") LocalDate fromDate,
                        @org.springframework.data.repository.query.Param("status") ServiceBookingStatus status
        );

        @org.springframework.data.jpa.repository.Query("""
                select b from ServiceBooking b
                    join fetch b.contract c
                    join fetch c.room r
                    join fetch c.tenant t
                    join fetch b.service s
                 where b.id = :id
                """)
        java.util.Optional<ServiceBooking> findByIdWithContractRoomTenantService(
                        @org.springframework.data.repository.query.Param("id") Long id
        );
}
