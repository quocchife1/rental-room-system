package com.example.rental.service;

import com.example.rental.dto.contractservice.ContractServiceRequest;
import com.example.rental.dto.contractservice.ContractServiceResponse;
import java.util.List;

public interface ContractServiceService {
    ContractServiceResponse addServiceToContract(Long contractId, ContractServiceRequest request);
    List<ContractServiceResponse> getServicesByContract(Long contractId);
    void removeService(Long id);

    ContractServiceResponse cancelServiceEffectiveEndOfMonth(Long contractId, Long contractServiceId);

    ContractServiceResponse updateMeterReadings(Long contractId, Long contractServiceId, com.example.rental.dto.contractservice.MeterReadingUpdateRequest request);
}
