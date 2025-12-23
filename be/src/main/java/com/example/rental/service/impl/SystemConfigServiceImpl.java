package com.example.rental.service.impl;

import com.example.rental.dto.system.SystemConfigDto;
import com.example.rental.dto.system.SystemConfigUpsertRequest;
import com.example.rental.entity.SystemConfig;
import com.example.rental.repository.SystemConfigRepository;
import com.example.rental.security.Audited;
import com.example.rental.entity.AuditAction;
import com.example.rental.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final Long SINGLETON_ID = 1L;

    private final SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional(readOnly = true)
    public SystemConfigDto get() {
        SystemConfig entity = systemConfigRepository.findById(SINGLETON_ID)
                .orElseGet(() -> systemConfigRepository.save(SystemConfig.builder().id(SINGLETON_ID).build()));
        return toDto(entity);
    }

    @Override
    @Audited(action = AuditAction.UPDATE_PRICE, targetType = "SYSTEM_CONFIG", description = "Cập nhật cấu hình hệ thống")
    public SystemConfigDto upsert(SystemConfigUpsertRequest request) {
        SystemConfig entity = systemConfigRepository.findById(SINGLETON_ID)
                .orElseGet(() -> SystemConfig.builder().id(SINGLETON_ID).build());

        entity.setElectricPricePerUnit(request.getElectricPricePerUnit());
        entity.setWaterPricePerUnit(request.getWaterPricePerUnit());
        entity.setLateFeePerDay(request.getLateFeePerDay());

        entity.setMomoReceiverName(request.getMomoReceiverName());
        entity.setMomoReceiverPhone(request.getMomoReceiverPhone());
        entity.setMomoReceiverQrUrl(request.getMomoReceiverQrUrl());

        entity = systemConfigRepository.save(entity);
        return toDto(entity);
    }

    private static SystemConfigDto toDto(SystemConfig entity) {
        return SystemConfigDto.builder()
                .electricPricePerUnit(entity.getElectricPricePerUnit())
                .waterPricePerUnit(entity.getWaterPricePerUnit())
                .lateFeePerDay(entity.getLateFeePerDay())
                .momoReceiverName(entity.getMomoReceiverName())
                .momoReceiverPhone(entity.getMomoReceiverPhone())
                .momoReceiverQrUrl(entity.getMomoReceiverQrUrl())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
