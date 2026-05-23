package com.example.rental.service.impl;

import com.example.rental.dto.system.SystemConfigDto;
import com.example.rental.dto.system.SystemConfigUpsertRequest;
import com.example.rental.entity.SystemConfig;
import com.example.rental.repository.SystemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemConfigServiceImpl Tests")
class SystemConfigServiceImplTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private SystemConfigServiceImpl systemConfigService;

    private static final Long SINGLETON_ID = 1L;

    @Test
    @DisplayName("Get Config - Should return existing config")
    void get_ExistingConfig() {
        SystemConfig config = SystemConfig.builder()
                .id(SINGLETON_ID)
                .electricPricePerUnit(new BigDecimal("3500"))
                .build();
        
        when(systemConfigRepository.findById(SINGLETON_ID)).thenReturn(Optional.of(config));

        SystemConfigDto result = systemConfigService.get();

        assertThat(result.getElectricPricePerUnit()).isEqualByComparingTo("3500");
        verify(systemConfigRepository, times(1)).findById(SINGLETON_ID);
    }

    @Test
    @DisplayName("Get Config - Should create new if not exists")
    void get_CreateIfMissing() {
        when(systemConfigRepository.findById(SINGLETON_ID)).thenReturn(Optional.empty());
        when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(i -> i.getArgument(0));

        SystemConfigDto result = systemConfigService.get();

        assertThat(result).isNotNull();
        verify(systemConfigRepository).save(any(SystemConfig.class));
    }

    @Test
    @DisplayName("Upsert Config - Success")
    void upsert_Success() {
        SystemConfigUpsertRequest request = new SystemConfigUpsertRequest();
        request.setElectricPricePerUnit(new BigDecimal("4000"));
        request.setWaterPricePerUnit(new BigDecimal("20000"));
        request.setMomoReceiverName("NGUYEN QUOC CHI");

        SystemConfig entity = SystemConfig.builder().id(SINGLETON_ID).build();
        when(systemConfigRepository.findById(SINGLETON_ID)).thenReturn(Optional.of(entity));
        when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(i -> i.getArgument(0));

        SystemConfigDto result = systemConfigService.upsert(request);

        assertThat(result.getElectricPricePerUnit()).isEqualByComparingTo("4000");
        assertThat(result.getMomoReceiverName()).isEqualTo("NGUYEN QUOC CHI");
        verify(systemConfigRepository).save(entity);
    }
}
