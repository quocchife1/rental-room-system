package com.example.rental.service.impl;

import com.example.rental.dto.maintenance.*;
import com.example.rental.dto.invoice.InvoiceResponse;
import com.example.rental.entity.*;
import com.example.rental.mapper.MaintenanceMapper;
import com.example.rental.repository.*;
import com.example.rental.service.InvoiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MaintenanceRequestServiceImpl Tests")
class MaintenanceRequestServiceImplTest {

    @Mock private MaintenanceRequestRepository maintenanceRequestRepository;
    @Mock private MaintenanceImageRepository maintenanceImageRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private InvoiceService invoiceService;
    @Mock private MaintenanceMapper maintenanceMapper;

    @InjectMocks
    private MaintenanceRequestServiceImpl maintenanceRequestService;

    @Test
    @DisplayName("Create Request - Success with Image")
    void createRequest_SuccessWithImage() throws IOException {
        MaintenanceRequestCreate request = new MaintenanceRequestCreate();
        request.setTenantId(1L);
        request.setBranchCode("B01");
        request.setRoomNumber("101");
        request.setImages(new MultipartFile[]{ new MockMultipartFile("img", "test.jpg", "image/jpeg", "data".getBytes()) });

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(new Tenant()));
        when(roomRepository.findByBranchCodeAndRoomNumber("B01", "101")).thenReturn(Optional.of(new Room()));
        when(maintenanceRequestRepository.save(any())).thenReturn(MaintenanceRequest.builder().id(1L).build());
        when(maintenanceMapper.toResponse(any())).thenReturn(MaintenanceResponse.builder().build());

        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            var uriBuilder = mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(uriBuilder);
            when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.toUriString()).thenReturn("http://localhost/uploads/test.jpg");

            maintenanceRequestService.createRequest(request);
            verify(maintenanceRequestRepository).save(any());
        }
    }

    @Test
    @DisplayName("Create Tenant Fault Invoice - Success")
    void createTenantFaultInvoice_Success() {
        MaintenanceRequest req = MaintenanceRequest.builder()
                .id(1L)
                .room(Room.builder().id(1L).build())
                .build();

        when(maintenanceRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(req));

        when(contractRepository.findByRoomIdAndStatus(anyLong(), any()))
                .thenReturn(Contract.builder().id(1L).build());

        // FIX: InvoiceResponse phải có id != null để entity.setInvoiceId(invoice.getId()) không set null
        when(invoiceService.createMaintenanceInvoice(any(), any(), any(), any()))
                .thenReturn(InvoiceResponse.builder().id(99L).build());

        MaintenanceInvoiceCreateRequest invoiceRequest = new MaintenanceInvoiceCreateRequest();
        invoiceRequest.setAmount(new BigDecimal("100000"));

        maintenanceRequestService.createTenantFaultInvoice(1L, invoiceRequest);

        verify(maintenanceRequestRepository).save(any(MaintenanceRequest.class));
        assertThat(req.getInvoiceId()).isNotNull();
    }
}