package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.system.SystemConfigDto;
import com.example.rental.dto.system.SystemConfigUpsertRequest;
import com.example.rental.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system-config")
@RequiredArgsConstructor
@Tag(name = "System Config")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    @Operation(summary = "Get system configuration")
    public ResponseEntity<ApiResponseDto<SystemConfigDto>> get() {
        return ResponseEntity.ok(ApiResponseDto.success(200, "Config fetched", systemConfigService.get()));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'MANAGER')")
    @Operation(summary = "Update system configuration")
    public ResponseEntity<ApiResponseDto<SystemConfigDto>> upsert(@RequestBody SystemConfigUpsertRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(200, "Config updated", systemConfigService.upsert(request)));
    }
}
