package com.example.rental.repository;

import com.example.rental.entity.PartnerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerPaymentRepository extends JpaRepository<PartnerPayment, Long> {
    // Tìm thanh toán theo ID đối tác
    List<PartnerPayment> findByPartnerId(Long partnerId);
}