package com.example.rental.service.impl;

import com.example.rental.dto.contractservice.ContractServiceRequest;
import com.example.rental.dto.contractservice.ContractServiceResponse;
import com.example.rental.dto.contractservice.MeterReadingUpdateRequest;
import com.example.rental.entity.*;
import com.example.rental.mapper.ContractServiceMapper;
import com.example.rental.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractServiceServiceImpl Tests")
class ContractServiceServiceImplTest {

    @Mock private ContractRepository contractRepository;
    @Mock private ContractServiceRepository contractServiceRepository;
    @Mock private RentalServiceRepository rentalServiceRepository;

    @InjectMocks
    private ContractServiceServiceImpl contractServiceService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Add Service - Success")
    void addService_Success() {
        Contract contract = Contract.builder().id(1L).build();
        RentalServiceItem service = new RentalServiceItem();
        service.setServiceName("Giữ xe máy");

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(rentalServiceRepository.findById(10L)).thenReturn(Optional.of(service));

        ContractServiceRequest req = new ContractServiceRequest();
        req.setServiceId(10L);
        req.setQuantity(2);

        try (MockedStatic<ContractServiceMapper> mapper = mockStatic(ContractServiceMapper.class)) {
            ContractService entity = new ContractService();
            mapper.when(() -> ContractServiceMapper.toEntity(any(), any())).thenReturn(entity);
            mapper.when(() -> ContractServiceMapper.toResponse(any())).thenReturn(ContractServiceResponse.builder().build());

            when(contractServiceRepository.save(any())).thenReturn(entity);

            contractServiceService.addServiceToContract(1L, req);
            
            verify(contractServiceRepository).save(any());
        }
    }

    @Test
    @DisplayName("Update Meter Readings - Success")
    void updateMeterReadings_Success() {
        Contract contract = Contract.builder().id(1L).build();
        // Cần gán contract đầy đủ vào ContractService để tránh NullPointerException
        ContractService cs = ContractService.builder().contract(contract).build(); 
        
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractServiceRepository.findById(2L)).thenReturn(Optional.of(cs));
        when(contractServiceRepository.save(any())).thenReturn(cs);

        MeterReadingUpdateRequest req = new MeterReadingUpdateRequest();
        req.setPreviousReading(new java.math.BigDecimal("100.0"));
        req.setCurrentReading(new java.math.BigDecimal("150.0"));

        try (MockedStatic<ContractServiceMapper> mapper = mockStatic(ContractServiceMapper.class)) {
            mapper.when(() -> ContractServiceMapper.toResponse(any())).thenReturn(ContractServiceResponse.builder().build());
            
            contractServiceService.updateMeterReadings(1L, 2L, req);
            
            assertThat(cs.getPreviousReading()).isEqualByComparingTo("100.0");
            assertThat(cs.getCurrentReading()).isEqualByComparingTo("150.0");
        }
    }

    @Test
    @DisplayName("Tenant Ownership - Throw Exception if not owner")
    void assertTenantOwnsContract_ThrowsException() {
        // Setup SecurityContext với ROLE_TENANT
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("wrongUser", null, List.of(new SimpleGrantedAuthority("ROLE_TENANT")))
        );

        // realTenant khác với wrongUser
        Tenant realTenant = Tenant.builder().username("realTenant").build();
        Contract contract = Contract.builder().id(1L).tenant(realTenant).build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        // Kiểm tra xem Service có ném ngoại lệ khi user không khớp không
        assertThatThrownBy(() -> contractServiceService.getServicesByContract(1L))
                .hasMessageContaining("Bạn không có quyền thao tác trên hợp đồng này");
    }
}