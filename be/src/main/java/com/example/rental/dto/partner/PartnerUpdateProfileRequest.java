package com.example.rental.dto.partner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartnerUpdateProfileRequest {

    @Schema(description = "Tên công ty/chủ trọ", example = "Công ty TNHH Dịch vụ Phòng trọ Z")
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    @Schema(description = "Mã số thuế", example = "0398765432")
    private String taxCode;

    @Schema(description = "Người liên hệ", example = "Trần Thị C")
    @NotBlank(message = "Người liên hệ không được để trống")
    private String contactPerson;

    @Schema(description = "Địa chỉ đăng ký kinh doanh/nơi ở", example = "789 Đường Hai Bà Trưng, Quận 3")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
}