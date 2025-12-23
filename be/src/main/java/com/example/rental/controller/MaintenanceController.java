package com.example.rental.controller;

import com.example.rental.dto.maintenance.*;
import com.example.rental.service.MaintenanceRequestService;
import com.example.rental.repository.TenantRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceRequestService maintenanceRequestService;
    private final TenantRepository tenantRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<MaintenanceResponse>> createRequest(
            @RequestPart(value = "tenantName", required = false) String tenantName,
            @RequestPart(value = "branchCode", required = false) String branchCode,
            @RequestPart(value = "roomNumber", required = false) String roomNumber,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        MaintenanceRequestCreate request = new MaintenanceRequestCreate();
        request.setTenantName(tenantName);
        request.setBranchCode(branchCode);
        request.setRoomNumber(roomNumber);
        request.setDescription(description);
        request.setImages(images);

        // Prefer tenantId from authenticated principal when available
        Long tenantId = null;
        try {
            String current = SecurityContextHolder.getContext().getAuthentication().getName();
            if (current != null && !"anonymousUser".equals(current)) {
                Optional<com.example.rental.entity.Tenant> t = tenantRepository.findByUsernameIgnoreCase(current);
                if (t.isPresent()) tenantId = t.get().getId();
            }
        } catch (Exception ex) {
            // ignore, we'll fallback to tenantName lookup in service
        }

        request.setTenantId(tenantId);

        MaintenanceResponse resp = maintenanceRequestService.createRequest(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
            .body(com.example.rental.dto.ApiResponseDto.success(201, "Maintenance request created", resp));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<MaintenanceResponse>>> getMyRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var tenantOpt = tenantRepository.findByUsernameIgnoreCase(username);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(com.example.rental.dto.ApiResponseDto.success(404, "Tenant not found", null));
        }
        var list = maintenanceRequestService.getRequestsByTenant(tenantOpt.get().getId());
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Maintenance requests fetched", list));
    }

    @GetMapping("/board")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAINTENANCE')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<MaintenanceResponse>>> getBoard() {
        var list = maintenanceRequestService.getAllRequests();
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Maintenance board fetched", list));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAINTENANCE')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<MaintenanceResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody MaintenanceStatusUpdateRequest request) {
        MaintenanceResponse resp = maintenanceRequestService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Maintenance status updated", resp));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<MaintenanceResponse>>> getRequestsByTenant(@PathVariable Long tenantId) {
        java.util.List<MaintenanceResponse> list = maintenanceRequestService.getRequestsByTenant(tenantId);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Maintenance requests fetched", list));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<MaintenanceResponse>>> getRequestsByStatus(@PathVariable String status) {
        java.util.List<MaintenanceResponse> list = maintenanceRequestService.getRequestsByStatus(status);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Maintenance requests fetched", list));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAINTENANCE')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<MaintenanceResponse>> updateRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String resolution,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String technician,
            @RequestParam(required = false) String cost) {
        MaintenanceResponse resp = maintenanceRequestService.updateRequest(id, resolution, status, technician, cost);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Maintenance request updated", resp));
    }

    @PostMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MAINTENANCE')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<com.example.rental.dto.maintenance.MaintenanceInvoiceCreateResponse>> createInvoiceForTenantFault(
            @PathVariable Long id,
            @RequestBody com.example.rental.dto.maintenance.MaintenanceInvoiceCreateRequest request
    ) {
        var resp = maintenanceRequestService.createTenantFaultInvoice(id, request);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Maintenance invoice created", resp));
    }
}
