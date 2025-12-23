package com.example.rental.service.impl;

import com.example.rental.dto.rentalservice.RentalServiceRequest;
import com.example.rental.dto.rentalservice.RentalServiceResponse;
import com.example.rental.entity.RentalServiceItem;
import com.example.rental.mapper.RentalServiceMapper;
import com.example.rental.repository.RentalServiceRepository;
import com.example.rental.service.RentalServiceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalServiceServiceImpl implements RentalServiceService {

    private final RentalServiceRepository rentalServiceRepository;

    @Override
    public RentalServiceResponse create(RentalServiceRequest request) {
        RentalServiceItem entity = RentalServiceMapper.toEntity(request);
        return RentalServiceMapper.toResponse(rentalServiceRepository.save(entity));
    }

    @Override
    public RentalServiceResponse update(Long id, RentalServiceRequest request) {
        RentalServiceItem item = rentalServiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        item.setServiceName(request.getServiceName());
        item.setPrice(request.getPrice());
        item.setUnit(request.getUnit());
        item.setDescription(request.getDescription());

        return RentalServiceMapper.toResponse(rentalServiceRepository.save(item));
    }

    @Override
    public void delete(Long id) {
        if (!rentalServiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Service not found");
        }
        rentalServiceRepository.deleteById(id);
    }

    @Override
    public RentalServiceResponse getById(Long id) {
        RentalServiceItem item = rentalServiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        return RentalServiceMapper.toResponse(item);
    }

    @Override
    public List<RentalServiceResponse> getAll() {
        return rentalServiceRepository.findAll().stream()
                .map(RentalServiceMapper::toResponse)
                .collect(Collectors.toList());
    }
}
