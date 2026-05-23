package com.example.rental.service.impl;

import com.example.rental.entity.ServiceItem;
import com.example.rental.repository.ServiceItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceItemServiceImpl Tests")
class ServiceItemServiceImplTest {

    @Mock
    private ServiceItemRepository repo;

    @InjectMocks
    private ServiceItemServiceImpl service;

    @Test
    @DisplayName("Create ServiceItem - Success")
    void create_Success() {
        ServiceItem item = new ServiceItem();
        when(repo.save(item)).thenReturn(item);

        ServiceItem result = service.create(item);

        assertThat(result).isEqualTo(item);
        verify(repo, times(1)).save(item);
    }

    @Test
    @DisplayName("Get By Id - Found")
    void getById_Found() {
        ServiceItem item = new ServiceItem();
        when(repo.findById(1L)).thenReturn(Optional.of(item));

        ServiceItem result = service.getById(1L);

        assertThat(result).isEqualTo(item);
    }

    @Test
    @DisplayName("Get By Id - Not Found")
    void getById_NotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        ServiceItem result = service.getById(1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Get All - Success")
    void getAll_Success() {
        when(repo.findAll()).thenReturn(List.of(new ServiceItem()));

        List<ServiceItem> results = service.getAll();

        assertThat(results).hasSize(1);
        verify(repo).findAll();
    }

    @Test
    @DisplayName("Delete - Success")
    void delete_Success() {
        doNothing().when(repo).deleteById(1L);

        service.delete(1L);

        verify(repo, times(1)).deleteById(1L);
    }
}
