package com.example.rental.service;

import com.example.rental.dto.maintenance.*;

import java.util.List;

public interface MaintenanceRequestService {
    MaintenanceResponse createRequest(MaintenanceRequestCreate request);

    List<MaintenanceResponse> getRequestsByTenant(Long tenantId);

    List<MaintenanceResponse> getRequestsByStatus(String status);

    List<MaintenanceResponse> getAllRequests();

    MaintenanceResponse updateRequest(Long requestId, String resolution, String status, String technician, String cost);

    MaintenanceResponse updateStatus(Long requestId, String status);

    com.example.rental.dto.maintenance.MaintenanceInvoiceCreateResponse createTenantFaultInvoice(
            Long requestId,
            com.example.rental.dto.maintenance.MaintenanceInvoiceCreateRequest request
    );
}
