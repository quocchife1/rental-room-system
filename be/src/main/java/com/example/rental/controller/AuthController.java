package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.auth.*;
import com.example.rental.entity.AuditAction;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API quản lý đăng ký, đăng nhập cho các loại tài khoản (Guest, Tenant, Partner, Employee)")
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    @Operation(summary = "Đăng ký khách vãng lai (Sinh viên)")
    @PostMapping("/register/guest")
    public ResponseEntity<ApiResponseDto<Void>> registerGuest(@Valid @RequestBody GuestRegisterRequest request) {
        authService.registerGuest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Đăng ký tài khoản khách thành công"));
    }

    @Operation(summary = "Đăng ký người thuê (Tenant)")
    @PostMapping("/register/tenant")
    public ResponseEntity<ApiResponseDto<Void>> registerTenant(@Valid @RequestBody TenantRegisterRequest request) {
        authService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Tenant registered successfully"));
    }

    @Operation(summary = "Đăng ký đối tác (Chủ trọ)")
    @PostMapping("/register/partner")
    public ResponseEntity<ApiResponseDto<Void>> registerPartner(@Valid @RequestBody PartnerRegisterRequest request) {
        authService.registerPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Đăng ký tài khoản chủ trọ thành công"));
    }

    @Operation(summary = "Đăng ký nhân viên (Admin tạo tài khoản)")
    @PostMapping("/register/employee")
    public ResponseEntity<ApiResponseDto<Void>> registerEmployee(@Valid @RequestBody EmployeeRegisterRequest request) {
        authService.registerEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Employee registered successfully"));
    }

    @Operation(summary = "Đăng nhập hệ thống")
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponse>> login(@Valid @RequestBody AuthLoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Login successful", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<String>> logout(org.springframework.security.core.Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "ANONYMOUS";
        String role = "ANONYMOUS";
        if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }

        auditLogService.logAction(
                username,
                role,
                AuditAction.LOGOUT,
                "AUTH",
                null,
                "Đăng xuất",
                null,
                null,
                "unknown",
                null,
                "unknown",
                "SUCCESS",
                null
        );

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Logout logged", "OK"));
    }
}
