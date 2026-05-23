package com.example.rental.service.impl;

import com.example.rental.dto.rentalservice.RentalServiceRequest;
import com.example.rental.dto.rentalservice.RentalServiceResponse;
import com.example.rental.entity.RentalServiceItem;
import com.example.rental.mapper.RentalServiceMapper;
import com.example.rental.repository.RentalServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalServiceServiceImpl Tests")
class RentalServiceServiceImplTest {

    @Mock private RentalServiceRepository rentalServiceRepository;
    @InjectMocks private RentalServiceServiceImpl rentalServiceService;

    @Test
    @DisplayName("Create Rental Service - Success")
    void create_Success() {
        RentalServiceRequest request = new RentalServiceRequest();
        RentalServiceItem entity = new RentalServiceItem();
        
        try (MockedStatic<RentalServiceMapper> mapper = mockStatic(RentalServiceMapper.class)) {
            mapper.when(() -> RentalServiceMapper.toEntity(request)).thenReturn(entity);
            mapper.when(() -> RentalServiceMapper.toResponse(any())).thenReturn(RentalServiceResponse.builder().build());

            when(rentalServiceRepository.save(entity)).thenReturn(entity);

            RentalServiceResponse response = rentalServiceService.create(request);

            assertThat(response).isNotNull();
            verify(rentalServiceRepository).save(entity);
        }
    }

    @Test
    @DisplayName("Update Rental Service - Success")
    void update_Success() {
        Long id = 1L;
        RentalServiceRequest request = new RentalServiceRequest();
        request.setServiceName("New Name");
        request.setPrice(new BigDecimal("1000"));

        RentalServiceItem item = new RentalServiceItem();
        when(rentalServiceRepository.findById(id)).thenReturn(Optional.of(item));
        when(rentalServiceRepository.save(any())).thenReturn(item);

        try (MockedStatic<RentalServiceMapper> mapper = mockStatic(RentalServiceMapper.class)) {
            mapper.when(() -> RentalServiceMapper.toResponse(any())).thenReturn(RentalServiceResponse.builder().build());

            rentalServiceService.update(id, request);

            assertThat(item.getServiceName()).isEqualTo("New Name");
            verify(rentalServiceRepository).save(item);
        }
    }

    @Test
    @DisplayName("Delete Rental Service - Success")
    void delete_Success() {
        Long id = 1L;
        when(rentalServiceRepository.existsById(id)).thenReturn(true);

        rentalServiceService.delete(id);

        verify(rentalServiceRepository).deleteById(id);
    }

    @Test
    @DisplayName("Delete Rental Service - Not Found")
    void delete_NotFound() {
        when(rentalServiceRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> rentalServiceService.delete(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Get All - Returns List")
    void getAll_ReturnsList() {
        when(rentalServiceRepository.findAll()).thenReturn(List.of(new RentalServiceItem()));

        try (MockedStatic<RentalServiceMapper> mapper = mockStatic(RentalServiceMapper.class)) {
            mapper.when(() -> RentalServiceMapper.toResponse(any())).thenReturn(RentalServiceResponse.builder().build());

            List<RentalServiceResponse> results = rentalServiceService.getAll();

            assertThat(results).hasSize(1);
            verify(rentalServiceRepository).findAll();
        }
    }
}
