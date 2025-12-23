package com.example.rental.repository;

import com.example.rental.entity.Employees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional; 

@Repository
public interface EmployeeRepository extends JpaRepository<Employees, Long> {
    
    // Đã có sẵn: Trả về Optional
    Optional<Employees> findByUsername(String username);

    Optional<Employees> findByUsernameIgnoreCase(String username);

    // Đã có sẵn: Trả về Optional
    Optional<Employees> findByEmployeeCode(String employeeCode);

    // NEW: Bổ sung cho hàm checkUserExistence trong AuthServiceImpl
    boolean existsByUsername(String username);

    // NEW: Bổ sung cho hàm checkUserExistence trong AuthServiceImpl
    boolean existsByEmail(String email);

    @Modifying
    @Query("update Employees e set e.branch = null where lower(e.branch.branchCode) = lower(:branchCode)")
    void unassignBranchByBranchCode(@Param("branchCode") String branchCode);
}