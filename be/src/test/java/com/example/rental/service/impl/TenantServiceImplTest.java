package com.example.rental.service.impl;

import com.example.rental.dto.auth.AuthRegisterRequest;
import com.example.rental.dto.tenant.TenantResponse;
import com.example.rental.dto.tenant.TenantUpdateProfileRequest;
import com.example.rental.entity.Tenant;
import com.example.rental.entity.UserStatus;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.TenantMapper;
import com.example.rental.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantServiceImpl Tests")
class TenantServiceImplTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TenantMapper tenantMapper;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Test
    @DisplayName("Register New Tenant - Success")
    void registerNewTenant_Success() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setPassword("plainPassword");
        
        Tenant tenant = new Tenant();
        TenantResponse response = TenantResponse.builder().build();

        when(tenantMapper.registerRequestToTenant(request)).thenReturn(tenant);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantMapper.tenantToTenantResponse(tenant)).thenReturn(response);

        TenantResponse result = tenantService.registerNewTenant(request);

        assertThat(result).isNotNull();
        assertThat(tenant.getPassword()).isEqualTo("encodedPassword");
        assertThat(tenant.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(tenantRepository).save(tenant);
    }

    @Test
    @DisplayName("Update Profile - Success")
    void updateTenantProfile_Success() {
        Long tenantId = 1L;
        TenantUpdateProfileRequest request = new TenantUpdateProfileRequest();
        Tenant existingTenant = new Tenant();
        
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(existingTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(existingTenant);
        when(tenantMapper.tenantToTenantResponse(existingTenant)).thenReturn(TenantResponse.builder().build());

        tenantService.updateTenantProfile(tenantId, request);

        verify(tenantMapper).updateTenantFromDto(request, existingTenant);
        verify(tenantRepository).save(existingTenant);
    }

    @Test
    @DisplayName("Update Profile - Throw ResourceNotFoundException")
    void updateTenantProfile_NotFound() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.updateTenantProfile(1L, new TenantUpdateProfileRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Find By Username - Case Insensitive Logic")
    void findByUsername_ChecksBothCase() {
        String username = "testUser";
        Tenant tenant = new Tenant();
        
        // Giả lập tìm kiếm chính xác trả về empty, tìm kiếm không phân biệt hoa thường trả về kết quả
        when(tenantRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(tenantRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(tenant));

        Optional<Tenant> result = tenantService.findByUsername(username);

        assertThat(result).isPresent().contains(tenant);
    }
}
