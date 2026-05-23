package com.example.rental.service.impl;

import com.example.rental.dto.checkout.CheckoutRequestDto;
import com.example.rental.dto.checkout.CheckoutRequestResponse;
import com.example.rental.dto.invoice.InvoiceRequest;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import com.example.rental.service.InvoiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutServiceImpl Tests")
class CheckoutServiceImplTest {

    @Mock private CheckoutRequestRepository checkoutRequestRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private InvoiceService invoiceService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    @Test
    @DisplayName("Submit Checkout Request - Success")
    void submitCheckoutRequest_Success() {
        Long contractId = 1L;
        String username = "tenantUser";
        CheckoutRequestDto dto = new CheckoutRequestDto();
        dto.setReason("Moving out");

        Contract contract = new Contract();
        contract.setId(contractId);
        Tenant tenant = new Tenant();
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(tenantRepository.findByUsername(username)).thenReturn(Optional.of(tenant));
        when(checkoutRequestRepository.save(any(CheckoutRequest.class))).thenAnswer(i -> {
            CheckoutRequest cr = i.getArgument(0);
            cr.setId(100L); // Giả lập ID sau khi save
            return cr;
        });

        CheckoutRequestResponse response = checkoutService.submitCheckoutRequest(contractId, username, dto);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(checkoutRequestRepository).save(any(CheckoutRequest.class));
    }

    @Test
    @DisplayName("Approve Request - Success")
    void approveRequest_Success() {
        CheckoutRequest req = new CheckoutRequest();
        req.setId(1L);
        req.setStatus(CheckoutStatus.PENDING);
        req.setContract(new Contract());
        req.setTenant(new Tenant());

        when(checkoutRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        CheckoutRequestResponse response = checkoutService.approveRequest(1L, "admin");

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        verify(checkoutRequestRepository).save(req);
    }

    @Test
    @DisplayName("Finalize Checkout - Success")
    void finalizeCheckout_Success() {
        Contract contract = new Contract();
        contract.setId(1L);
        Room room = new Room();
        contract.setRoom(room);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        checkoutService.finalizeCheckout(1L, "admin");

        verify(invoiceService).create(any(InvoiceRequest.class));
        verify(contractRepository).save(contract);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ENDED);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Submit Checkout Request - Contract Not Found")
    void submitCheckoutRequest_ContractNotFound() {
        when(contractRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.submitCheckoutRequest(1L, "user", new CheckoutRequestDto()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Không tìm thấy hợp đồng");
    }
}
