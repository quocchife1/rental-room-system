package com.example.rental.mapper;

import com.example.rental.entity.MaintenanceRequest;
import com.example.rental.dto.maintenance.*;
import org.springframework.stereotype.Component; // <-- Import quan trọng

import java.util.Objects;
import java.util.stream.Collectors;

@Component // <-- Thêm annotation này
public class MaintenanceMapper {
    
    // Bỏ từ khóa 'static'
    public MaintenanceResponse toResponse(MaintenanceRequest entity) {
        String branchCode = null;
        String branchName = null;
        if (entity.getRoom() != null) {
            branchCode = entity.getRoom().getBranchCode();
            if (entity.getRoom().getBranch() != null) {
                branchName = entity.getRoom().getBranch().getBranchName();
            }
        }

        return MaintenanceResponse.builder()
                .id(entity.getId())
                .requestCode(entity.getRequestCode())
                .tenantName(entity.getTenant() != null ? entity.getTenant().getFullName() : "N/A") // Null check an toàn
                .branchCode(branchCode)
                .branchName(branchName)
                .roomNumber(entity.getRoom() != null ? entity.getRoom().getRoomNumber() : "N/A")
                .description(entity.getDescription())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .resolution(entity.getResolution())
                .cost(entity.getCost())
                .technicianName(entity.getTechnicianName())
            .invoiceId(entity.getInvoiceId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .images(entity.getImages() != null ?
                    entity.getImages().stream()
                        .filter(Objects::nonNull)
                        .map(i -> i.getImageUrl())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }
}