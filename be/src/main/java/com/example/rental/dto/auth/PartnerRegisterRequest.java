package com.example.rental.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerRegisterRequest extends AuthRegisterRequest {

    // Dùng Họ tên nhập từ Form làm Tên liên hệ
    @Schema(description = "Họ và tên người liên hệ", example = "Nguyễn Văn A")
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    // Các trường dưới đây có thể null, ta sẽ set mặc định trong Service
    @Schema(description = "Tên công ty/chủ trọ", example = "Nhà trọ X")
    private String companyName;

    @Schema(description = "Địa chỉ", example = "Hồ Chí Minh")
    private String address;

    @Schema(description = "Mã số thuế")
    private String taxCode;
}