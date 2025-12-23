package com.example.rental.dto.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantUpdateProfileRequest {

    @Schema(description = "Họ và tên người thuê", example = "Nguyễn Văn B")
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Schema(description = "Số điện thoại người thuê", example = "0901234567")
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @Schema(description = "Địa chỉ người thuê", example = "456 Đường Lê Lợi, Quận 1")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Schema(description = "Ngày sinh", example = "2000-01-01")
    @NotBlank(message = "Ngày sinh không được để trống")
    private String dob;
}