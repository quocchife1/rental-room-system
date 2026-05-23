package com.example.rental.service.impl;

import com.example.rental.dto.contract.ContractCreateRequest;
import com.example.rental.entity.*;
import com.example.rental.mapper.BranchMapper;
import com.example.rental.repository.*;
import com.example.rental.utils.ContractDocxGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ContractServiceImpl Tests")
class ContractServiceImplTest {

    @Mock private ContractRepository contractRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private ContractDocxGenerator contractDocxGenerator;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ContractServiceImpl contractService;

    @Test
    @DisplayName("Create Contract - Success")
    void createContract_Success() throws IOException {
        ContractCreateRequest request = new ContractCreateRequest();
        request.setBranchCode("B01");
        request.setRoomNumber("101");
        request.setTenantEmail("test@test.com");

        Branch branch = Branch.builder().branchCode("B01").build();
        Room room = Room.builder().roomNumber("101").id(1L).build();

        when(branchRepository.findByBranchCode("B01")).thenReturn(Optional.of(branch));
        when(roomRepository.findByBranchCodeAndRoomNumber("B01", "101")).thenReturn(Optional.of(room));
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            var uriBuilder = mock(ServletUriComponentsBuilder.class);
            var pathBuilder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(uriBuilder);
            when(uriBuilder.path(anyString())).thenReturn(pathBuilder);
            when(pathBuilder.toUriString()).thenReturn("http://localhost/contract.docx");
            when(contractDocxGenerator.generateContractFile(any(), any())).thenReturn("/uploads/contract.docx");

            Contract result = contractService.createContract(request);

            assertThat(result.getStatus()).isEqualTo(ContractStatus.PENDING);
            verify(roomRepository).save(any());
        }
    }
}