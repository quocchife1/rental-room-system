package com.example.rental.mapper;

import com.example.rental.dto.auth.AuthRegisterRequest;
import com.example.rental.dto.tenant.TenantResponse;
import com.example.rental.dto.tenant.TenantUpdateProfileRequest;
import com.example.rental.entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    // === 1. Đăng ký ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(source = "phone", target = "phoneNumber")
    Tenant registerRequestToTenant(AuthRegisterRequest registerRequest);

    // === 2. Entity sang Response DTO ===
    TenantResponse tenantToTenantResponse(Tenant tenant);

    // === 3. Cập nhật Profile ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateTenantFromDto(TenantUpdateProfileRequest request, @MappingTarget Tenant target);
}
