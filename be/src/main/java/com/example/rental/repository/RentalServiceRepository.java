package com.example.rental.repository;

import com.example.rental.entity.RentalServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalServiceRepository extends JpaRepository<RentalServiceItem, Long> {
	java.util.Optional<RentalServiceItem> findByServiceNameIgnoreCase(String serviceName);
}
