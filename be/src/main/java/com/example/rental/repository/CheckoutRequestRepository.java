package com.example.rental.repository;

import com.example.rental.entity.CheckoutRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CheckoutRequestRepository extends JpaRepository<CheckoutRequest, Long> {
    List<CheckoutRequest> findByTenantId(Long tenantId);
    List<CheckoutRequest> findByContractId(Long contractId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT cr FROM CheckoutRequest cr " +
            "JOIN cr.contract c " +
            "WHERE c.branchCode = :branchCode AND cr.status IN :statuses " +
            "ORDER BY cr.createdAt DESC"
    )
    Page<CheckoutRequest> findForBranchAndStatuses(
        @org.springframework.data.repository.query.Param("branchCode") String branchCode,
        @org.springframework.data.repository.query.Param("statuses") java.util.List<com.example.rental.entity.CheckoutStatus> statuses,
        Pageable pageable
    );
}
