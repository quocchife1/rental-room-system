package com.example.rental.service;

import com.example.rental.dto.auth.PartnerRegisterRequest;
import com.example.rental.dto.partner.PartnerResponse;
import com.example.rental.dto.partner.PartnerUpdateProfileRequest;
import com.example.rental.entity.Partners;
import java.util.List;
import java.util.Optional;

public interface PartnerService {
    // ĐÃ SỬA: Dùng DTO cho đăng ký
    PartnerResponse registerNewPartner(PartnerRegisterRequest request);
    
    // PHƯƠNG THỨC MỚI: Cập nhật bằng DTO
    PartnerResponse updatePartnerProfile(Long id, PartnerUpdateProfileRequest request);
    
    List<Partners> findAllPartners();
    Optional<Partners> findById(Long id);
    Optional<Partners> findByUsername(String username);
    Partners toggleStatus(Long partnerId);
}