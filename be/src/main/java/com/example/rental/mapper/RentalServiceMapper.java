package com.example.rental.mapper;

import com.example.rental.dto.rentalservice.RentalServiceRequest;
import com.example.rental.dto.rentalservice.RentalServiceResponse;
import com.example.rental.entity.RentalServiceItem;

public class RentalServiceMapper {

    public static RentalServiceItem toEntity(RentalServiceRequest request) {
        return RentalServiceItem.builder()
                .serviceName(request.getServiceName())
                .price(request.getPrice())
                .unit(request.getUnit())
                .description(request.getDescription())
                .build();
    }

    public static RentalServiceResponse toResponse(RentalServiceItem item) {
        return RentalServiceResponse.builder()
                .id(item.getId())
                .serviceName(item.getServiceName())
                .price(item.getPrice())
                .unit(item.getUnit())
                .description(item.getDescription())
                .build();
    }
}
