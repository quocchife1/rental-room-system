package com.example.rental.service;

import com.example.rental.entity.PartnerPayment;
import java.util.List;
import java.util.Optional;

public interface PartnerPaymentService {
    // Ghi nhận một thanh toán mới từ đối tác
    PartnerPayment recordPayment(PartnerPayment payment);

    // Lấy thanh toán theo ID
    Optional<PartnerPayment> findById(Long id);

    // Lấy danh sách thanh toán theo ID đối tác
    List<PartnerPayment> findPaymentsByPartnerId(Long partnerId);
}