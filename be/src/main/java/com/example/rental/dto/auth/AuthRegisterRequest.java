package com.example.rental.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthRegisterRequest {
    @Schema(description = "Tài khoản người dùng", example = "quocchi5523")
    @NotBlank(message = "Tài khoản người dùng không được để trống")
    private String username;

    @Schema(description = "Mật khẩu người dùng", example = "123456")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Schema(description = "Email người dùng", example = "chinguyen123852@gmail.com")
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Schema(description = "Số điện thoại người dùng", example = "0359444856")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
        regexp = "^(0|\\+84)(3[2-9]|5[25689]|7[0|6-9]|8[1-9]|9[0-9])\\d{7}$",
        message = "Số điện thoại không hợp lệ"
    )
    private String phone;
}