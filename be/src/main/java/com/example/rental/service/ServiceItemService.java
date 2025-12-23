package com.example.rental.service;

import com.example.rental.entity.ServiceItem;
import java.util.List;

public interface ServiceItemService {
    ServiceItem create(ServiceItem item);
    ServiceItem update(ServiceItem item);
    ServiceItem getById(Long id);
    List<ServiceItem> getAll();
    void delete(Long id);
}
