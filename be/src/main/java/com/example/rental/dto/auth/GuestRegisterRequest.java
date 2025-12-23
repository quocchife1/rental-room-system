package com.example.rental.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuestRegisterRequest extends AuthRegisterRequest {
    
    @Schema(description = "Họ và tên người dùng", example = "Nguyễn Quốc Chí")
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Schema(description = "Ngày sinh", example = "2005-05-05")
    private String dob; // Có thể để trống nếu không bắt buộc
}