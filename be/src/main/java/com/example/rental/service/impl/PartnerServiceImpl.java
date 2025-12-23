package com.example.rental.service.impl;

import com.example.rental.dto.auth.PartnerRegisterRequest;
import com.example.rental.dto.partner.PartnerResponse;
import com.example.rental.dto.partner.PartnerUpdateProfileRequest;
import com.example.rental.entity.Partners;
import com.example.rental.entity.UserStatus;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.PartnerMapper;
import com.example.rental.repository.PartnerRepository;
import com.example.rental.service.PartnerService;
import com.example.rental.service.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final PartnerMapper partnerMapper;

    @Override
    @Transactional
    public PartnerResponse registerNewPartner(PartnerRegisterRequest request) {
        // Convert request -> entity
        Partners newPartner = partnerMapper.toEntity(request);

        // Mã hoá password và set trạng thái mặc định
        newPartner.setPassword(passwordEncoder.encode(request.getPassword()));
        newPartner.setStatus(UserStatus.ACTIVE);

        // Lưu lần 1 để sinh ID
        Partners savedPartner = partnerRepository.save(newPartner);

        // Sinh partnerCode từ ID vừa có
        String partnerCode = codeGenerator.generateCode("DT", savedPartner.getId());
        savedPartner.setPartnerCode(partnerCode);

        // Lưu lần 2 sau khi có partnerCode
        Partners finalPartner = partnerRepository.save(savedPartner);

        return partnerMapper.toResponse(finalPartner);
    }

    @Override
    @Transactional
    public PartnerResponse updatePartnerProfile(Long id, PartnerUpdateProfileRequest request) {
        Partners existingPartner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", id));

        partnerMapper.updatePartnerFromDto(request, existingPartner);

        Partners updatedPartner = partnerRepository.save(existingPartner);
        return partnerMapper.toResponse(updatedPartner);
    }

    @Override
    public List<Partners> findAllPartners() {
        return partnerRepository.findAll();
    }

    @Override
    public Optional<Partners> findById(Long id) {
        return partnerRepository.findById(id);
    }

    @Override
    public Optional<Partners> findByUsername(String username) {
        return partnerRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public Partners toggleStatus(Long partnerId) {
        Partners partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", partnerId));

        if (partner.getStatus() == UserStatus.ACTIVE) {
            partner.setStatus(UserStatus.BANNED);
        } else {
            partner.setStatus(UserStatus.ACTIVE);
        }

        return partnerRepository.save(partner);
    }
}
