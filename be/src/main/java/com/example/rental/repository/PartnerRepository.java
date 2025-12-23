package com.example.rental.repository;

import com.example.rental.entity.Partners; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; 

@Repository
public interface PartnerRepository extends JpaRepository<Partners, Long> {
    
    // Đã có sẵn: Trả về Optional
    Optional<Partners> findByPartnerCode(String partnerCode);

    // Đã có sẵn: Trả về Optional<Partners>
    Optional<Partners> findByUsername(String username);

    // NEW: Bổ sung cho hàm checkUserExistence trong AuthServiceImpl
    boolean existsByUsername(String username);
    
    // NEW: Bổ sung cho hàm checkUserExistence trong AuthServiceImpl
    boolean existsByEmail(String email);
}