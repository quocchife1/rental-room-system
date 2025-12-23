package com.example.rental.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginRequest {
    @Schema(description = "Tài khoản người dùng", example = "quocchi5523")
    @NotBlank(message = "Tên người dùng không được để trống")
    private String username;

    @Schema(description = "Mật khẩu người dùng", example = "Quocchi0523@")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}