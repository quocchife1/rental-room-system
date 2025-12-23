package com.example.rental.repository;

import com.example.rental.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByUsername(String username);
    Optional<Tenant> findByUsernameIgnoreCase(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<Tenant> findByCccd(String cccd);
    boolean existsByCccd(String cccd);
    boolean existsByCccdAndIdNot(String cccd, Long id);
    boolean existsByStudentId(String studentId);
    boolean existsByStudentIdAndIdNot(String studentId, Long id);
    Optional<Tenant> findByFullName(String fullName);
}
