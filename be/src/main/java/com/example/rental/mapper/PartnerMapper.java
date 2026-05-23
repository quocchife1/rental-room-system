package com.example.rental.mapper;

import com.example.rental.dto.auth.PartnerRegisterRequest;
import com.example.rental.dto.partner.PartnerResponse;
import com.example.rental.dto.partner.PartnerUpdateProfileRequest;
import com.example.rental.entity.Partners;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartnerMapper {

    PartnerMapper INSTANCE = Mappers.getMapper(PartnerMapper.class);

    PartnerResponse toResponse(Partners partner);

    List<PartnerResponse> toResponseList(List<Partners> partners);

    /**
     * Tạo mới Partner từ PartnerRegisterRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "partnerCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "contactPerson", ignore = true) // <-- Bổ sung dòng này để sửa Warn 1
    @Mapping(source = "phone", target = "phoneNumber") 
    Partners toEntity(PartnerRegisterRequest request);

    /**
     * Update Partner từ PartnerUpdateProfileRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "partnerCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // <-- Bổ sung dòng này để sửa Warn 2
    @Mapping(target = "updatedAt", ignore = true) // <-- Bổ sung dòng này để sửa Warn 2
    
    // Xử lý Warn 2 cho phoneNumber:
    // NẾU DTO của bạn là 'phone', mở comment dòng dưới:
    // @Mapping(source = "phone", target = "phoneNumber") 
    // NẾU bạn cố tình không muốn update số điện thoại, mở comment dòng dưới:
    @Mapping(target = "phoneNumber", ignore = true)
    void updatePartnerFromDto(PartnerUpdateProfileRequest request, @MappingTarget Partners target);
}