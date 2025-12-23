package com.example.rental.repository;

import com.example.rental.entity.Contract;
import com.example.rental.entity.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    // Tìm kiếm hợp đồng theo ID người thuê
    List<Contract> findByTenantId(Long tenantId);

    org.springframework.data.domain.Page<Contract> findByTenantId(Long tenantId, Pageable pageable);

    // Tìm kiếm hợp đồng theo ID phòng
    Contract findByRoomIdAndStatus(Long roomId, ContractStatus status);

    java.util.List<Contract> findByRoom_IdIn(java.util.List<java.lang.Long> roomIds);

    // Fallback: lấy hợp đồng gần nhất của 1 phòng theo tenant (dùng khi không có ACTIVE)
    Contract findTopByRoomIdAndTenantIdOrderByCreatedAtDesc(Long roomId, Long tenantId);

        Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

        Page<Contract> findByBranchCode(String branchCode, Pageable pageable);

        Page<Contract> findByBranchCodeAndStatus(String branchCode, ContractStatus status, Pageable pageable);

        @Query("""
                        select c from Contract c
                        left join c.tenant t
                        where (:status is null or c.status = :status)
                            and (
                                    lower(coalesce(t.fullName,'')) like lower(concat('%', :q, '%'))
                                    or lower(coalesce(t.email,'')) like lower(concat('%', :q, '%'))
                                    or lower(coalesce(t.phoneNumber,'')) like lower(concat('%', :q, '%'))
                                    or lower(coalesce(c.roomNumber,'')) like lower(concat('%', :q, '%'))
                                    or lower(coalesce(c.branchCode,'')) like lower(concat('%', :q, '%'))
                            )
                        """)
        Page<Contract> searchGlobal(@Param("q") String q, @Param("status") ContractStatus status, Pageable pageable);

        @Query("""
                        select c from Contract c
                        left join c.tenant t
                        where lower(coalesce(c.branchCode,'')) = lower(:branchCode)
                            and (:status is null or c.status = :status)
                            and (
                                    lower(coalesce(t.fullName,'')) like lower(concat('%', :q, '%'))
                                    or lower(coalesce(t.email,'')) like lower(concat('%', :q, '%'))
                                    or lower(coalesce(t.phoneNumber,'')) like lower(concat('%', :q, '%'))
                                    or lower(coalesce(c.roomNumber,'')) like lower(concat('%', :q, '%'))
                            )
                        """)
        Page<Contract> searchInBranch(@Param("branchCode") String branchCode, @Param("q") String q, @Param("status") ContractStatus status, Pageable pageable);

        List<Contract> findByStatusAndEndDateAndEndReminderSentFalse(ContractStatus status, LocalDate endDate);

        List<Contract> findByStatus(ContractStatus status);
}