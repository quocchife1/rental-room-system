package com.example.rental.service.impl;

import com.example.rental.entity.ServiceItem;
import com.example.rental.repository.ServiceItemRepository;
import com.example.rental.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemRepository repo;

    @Override
    public ServiceItem create(ServiceItem item) {
        return repo.save(item);
    }

    @Override
    public ServiceItem update(ServiceItem item) {
        return repo.save(item);
    }

    @Override
    public ServiceItem getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public List<ServiceItem> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
