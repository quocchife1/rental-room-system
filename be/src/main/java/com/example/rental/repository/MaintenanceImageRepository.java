package com.example.rental.repository;

import com.example.rental.entity.MaintenanceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceImageRepository extends JpaRepository<MaintenanceImage, Long> {
}
