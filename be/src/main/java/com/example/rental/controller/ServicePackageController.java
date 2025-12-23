package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.servicepackage.ServicePackageResponse;
import com.example.rental.entity.ServicePackage;
import com.example.rental.repository.ServicePackageRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-packages")
@Tag(name = "Service Packages", description = "API danh mục gói dịch vụ cho tin đăng")
public class ServicePackageController {

    private final ServicePackageRepository servicePackageRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('PARTNER','ADMIN','MANAGER')")
    public ResponseEntity<ApiResponseDto<List<ServicePackageResponse>>> getActivePackages() {
        List<ServicePackage> list = servicePackageRepository.findByIsActiveTrueOrderByPriceAsc();
        List<ServicePackageResponse> response = list.stream()
                .map(p -> ServicePackageResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .price(p.getPrice())
                        .durationDays(p.getDurationDays())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy danh sách gói dịch vụ thành công", response));
    }
}
