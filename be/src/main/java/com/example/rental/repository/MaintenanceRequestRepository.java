package com.example.rental.repository;

import com.example.rental.entity.MaintenanceRequest;
import com.example.rental.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
    List<MaintenanceRequest> findByTenantId(Long tenantId);
    List<MaintenanceRequest> findByStatus(MaintenanceStatus status);

    void deleteByRoom_IdIn(java.util.List<java.lang.Long> roomIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mr from MaintenanceRequest mr where mr.id = :id")
    Optional<MaintenanceRequest> findByIdForUpdate(Long id);
}
