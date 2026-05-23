package com.example.rental.service.impl;

import com.example.rental.dto.auth.PartnerRegisterRequest;
import com.example.rental.dto.partner.PartnerResponse;
import com.example.rental.dto.partner.PartnerUpdateProfileRequest;
import com.example.rental.entity.Partners;
import com.example.rental.entity.UserStatus;
//import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.PartnerMapper;
import com.example.rental.repository.PartnerRepository;
import com.example.rental.service.util.CodeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PartnerServiceImpl Tests")
class PartnerServiceImplTest {

    @Mock private PartnerRepository partnerRepository;
    @Mock private CodeGenerator codeGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PartnerMapper partnerMapper;

    @InjectMocks
    private PartnerServiceImpl partnerService;

    @Test
    @DisplayName("Register New Partner - Success")
    void registerNewPartner_Success() {
        PartnerRegisterRequest request = new PartnerRegisterRequest();
        request.setPassword("rawPassword");

        Partners partner = new Partners();
        partner.setId(1L);
        PartnerResponse response = PartnerResponse.builder().build();

        when(partnerMapper.toEntity(request)).thenReturn(partner);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(partnerRepository.save(any(Partners.class))).thenReturn(partner);
        when(codeGenerator.generateCode(eq("DT"), any())).thenReturn("DT01");
        when(partnerMapper.toResponse(any())).thenReturn(response);

        PartnerResponse result = partnerService.registerNewPartner(request);

        assertThat(result).isNotNull();
        verify(partnerRepository, times(2)).save(any(Partners.class));
        assertThat(partner.getPassword()).isEqualTo("encodedPassword");
        assertThat(partner.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Update Partner Profile - Success")
    void updatePartnerProfile_Success() {
        Long partnerId = 1L;
        PartnerUpdateProfileRequest request = new PartnerUpdateProfileRequest();
        Partners existingPartner = new Partners();
        
        when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(existingPartner));
        when(partnerRepository.save(any(Partners.class))).thenReturn(existingPartner);
        when(partnerMapper.toResponse(any())).thenReturn(PartnerResponse.builder().build());

        partnerService.updatePartnerProfile(partnerId, request);

        verify(partnerMapper).updatePartnerFromDto(request, existingPartner);
        verify(partnerRepository).save(existingPartner);
    }

    @Test
    @DisplayName("Toggle Status - Active to Banned")
    void toggleStatus_ActiveToBanned() {
        Long partnerId = 1L;
        Partners partner = new Partners();
        partner.setStatus(UserStatus.ACTIVE);
        
        when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partners.class))).thenAnswer(i -> i.getArgument(0));

        Partners result = partnerService.toggleStatus(partnerId);

        assertThat(result.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    @DisplayName("Toggle Status - Banned to Active")
    void toggleStatus_BannedToActive() {
        Long partnerId = 1L;
        Partners partner = new Partners();
        partner.setStatus(UserStatus.BANNED);
        
        when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partners.class))).thenAnswer(i -> i.getArgument(0));

        Partners result = partnerService.toggleStatus(partnerId);

        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Find By Id - Resource Not Found")
    void findById_NotFound() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(partnerService.findById(1L)).isEmpty();
    }
}
