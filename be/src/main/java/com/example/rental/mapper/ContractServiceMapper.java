package com.example.rental.mapper;

import com.example.rental.dto.contractservice.ContractServiceRequest;
import com.example.rental.dto.contractservice.ContractServiceResponse;
import com.example.rental.entity.ContractService;
import com.example.rental.entity.RentalServiceItem;

public class ContractServiceMapper {

    public static ContractService toEntity(ContractServiceRequest req, RentalServiceItem service) {
        return ContractService.builder()
                .service(service)
                .quantity(req.getQuantity())
                .previousReading(req.getPreviousReading())
                .currentReading(req.getCurrentReading())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .build();
    }

    public static ContractServiceResponse toResponse(ContractService entity) {
        return ContractServiceResponse.builder()
                .id(entity.getId())
                .serviceName(entity.getService().getServiceName())
                .quantity(entity.getQuantity())
                .previousReading(entity.getPreviousReading())
                .currentReading(entity.getCurrentReading())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }
}
