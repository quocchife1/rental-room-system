package com.example.rental.dto.tenant;

import com.example.rental.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TenantResponse {

    private Long id;

    @Schema(description = "Tên đăng nhập", example = "quocchi5523")
    private String username;

    @Schema(description = "Họ và tên", example = "Nguyễn Quốc Chí")
    private String fullName;

    @Schema(description = "Email", example = "chinguyen123852@gmail.com")
    private String email;

    @Schema(description = "Số điện thoại", example = "0359444856")
    private String phoneNumber;

    @Schema(description = "Địa chỉ", example = "123 Nguyễn Trãi, TP.HCM")
    private String address;

    @Schema(description = "Ngày sinh", example = "2000-01-01")
    private String dob;

    @Schema(description = "Trạng thái người dùng", example = "ACTIVE")
    private UserStatus status;

}
