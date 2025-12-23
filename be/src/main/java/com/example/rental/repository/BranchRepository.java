package com.example.rental.repository;

import com.example.rental.entity.Branch;
import java.util.Optional; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    // Phương thức này TRẢ VỀ Optional<Branch>
    Optional<Branch> findByBranchCode(String branchCode);
}