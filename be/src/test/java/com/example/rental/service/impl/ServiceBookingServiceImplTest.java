package com.example.rental.service.impl;

import com.example.rental.dto.booking.CreateCleaningBookingRequest;
import com.example.rental.dto.booking.ServiceBookingResponse;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ServiceBookingServiceImpl Tests")
class ServiceBookingServiceImplTest {

    @Mock private ContractRepository contractRepository;
    @Mock private RentalServiceRepository rentalServiceRepository;
    @Mock private ServiceBookingRepository serviceBookingRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private ServiceBookingServiceImpl serviceBookingService;

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void mockTenant(String username) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_TENANT")))
        );
    }

    @Test
    @DisplayName("Create Cleaning Booking - Success (Next Thursday)")
    void createNextCleaningBooking_Success() {
        mockTenant("tenantUser");
        Contract contract = Contract.builder().id(1L).tenant(Tenant.builder().username("tenantUser").build()).build();
        
        RentalServiceItem service = RentalServiceItem.builder().serviceName("Vệ sinh").id(1L).build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(rentalServiceRepository.findByServiceNameIgnoreCase("Vệ sinh")).thenReturn(Optional.of(service));
        when(serviceBookingRepository.existsByContract_IdAndService_IdAndBookingDate(any(), any(), any())).thenReturn(false);
        when(serviceBookingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalDate nextThursday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
        CreateCleaningBookingRequest request = new CreateCleaningBookingRequest();
        request.setBookingDate(nextThursday);

        ServiceBookingResponse response = serviceBookingService.createNextCleaningBooking(1L, request);

        assertThat(response).isNotNull();
        verify(serviceBookingRepository).save(any(ServiceBooking.class));
    }

    @Test
    @DisplayName("Create Cleaning Booking - Fail on Wrong Day")
    void createNextCleaningBooking_FailWrongDay() {
        mockTenant("tenantUser");
        Contract contract = Contract.builder().id(1L).tenant(Tenant.builder().username("tenantUser").build()).build();
    
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(rentalServiceRepository.findByServiceNameIgnoreCase("Vệ sinh")).thenReturn(Optional.of(RentalServiceItem.builder().build()));
    
        CreateCleaningBookingRequest request = new CreateCleaningBookingRequest();
        request.setBookingDate(LocalDate.now().plusDays(1)); 
    
        // Kiểm tra bằng từ khóa tiếng Anh hoặc phần không dấu "Thu 5" thay vì "Thứ 5"
        assertThatThrownBy(() -> serviceBookingService.createNextCleaningBooking(1L, request))
                .isInstanceOf(RuntimeException.class);
    }
}