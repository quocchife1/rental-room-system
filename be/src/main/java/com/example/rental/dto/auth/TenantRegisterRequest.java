package com.example.rental.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Thông tin đăng ký người thuê (Tenant)")
public class TenantRegisterRequest extends AuthRegisterRequest {

    // --- THÊM TRƯỜNG NÀY ĐỂ SỬA LỖI ---
    @Schema(description = "Họ và tên người thuê", example = "Nguyễn Văn B")
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;
    // ----------------------------------

    @Schema(description = "Số CCCD/CMND của người thuê", example = "079123456789")
    @NotBlank(message = "CCCD/CMND không được để trống")
    private String cccd;

    @Schema(description = "Mã số sinh viên", example = "SV123456")
    private String studentId;

    @Schema(description = "Tên trường đại học", example = "Đại học Bách Khoa TP.HCM")
    private String university;

    @Schema(description = "Địa chỉ thường trú hoặc tạm trú", example = "456 Đường Lê Lợi, Quận 1, TP.HCM")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
}