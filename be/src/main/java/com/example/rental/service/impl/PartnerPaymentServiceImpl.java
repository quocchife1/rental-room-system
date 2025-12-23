package com.example.rental.service.impl;

import com.example.rental.entity.PartnerPayment;
import com.example.rental.repository.PartnerPaymentRepository;
import com.example.rental.service.PartnerPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartnerPaymentServiceImpl implements PartnerPaymentService {

    private final PartnerPaymentRepository partnerPaymentRepository;

    @Override
    @Transactional
    public PartnerPayment recordPayment(PartnerPayment payment) {
        // Logic nghiệp vụ: Có thể kiểm tra post_id có hợp lệ không
        // Logic nghiệp vụ: Cập nhật trạng thái tin đăng (nếu cần)
        return partnerPaymentRepository.save(payment);
    }

    @Override
    public Optional<PartnerPayment> findById(Long id) {
        return partnerPaymentRepository.findById(id);
    }

    @Override
    public List<PartnerPayment> findPaymentsByPartnerId(Long partnerId) {
        return partnerPaymentRepository.findByPartnerId(partnerId);
    }
}