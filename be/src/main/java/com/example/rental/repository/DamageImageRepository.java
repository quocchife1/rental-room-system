package com.example.rental.repository;

import com.example.rental.entity.DamageImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DamageImageRepository extends JpaRepository<DamageImage, Long> {
    List<DamageImage> findByDamageReportId(Long damageReportId);
}
