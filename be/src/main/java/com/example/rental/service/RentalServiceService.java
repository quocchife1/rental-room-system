package com.example.rental.service;

import com.example.rental.dto.rentalservice.RentalServiceRequest;
import com.example.rental.dto.rentalservice.RentalServiceResponse;

import java.util.List;

public interface RentalServiceService {
    RentalServiceResponse create(RentalServiceRequest request);
    RentalServiceResponse update(Long id, RentalServiceRequest request);
    void delete(Long id);
    RentalServiceResponse getById(Long id);
    List<RentalServiceResponse> getAll();
}
