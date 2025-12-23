package com.example.rental.dto.branch;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BranchRequest {

    @Schema(description = "Mã chi nhánh", example = "CN01")
    @NotBlank(message = "Mã chi nhánh không được để trống")
    private String branchCode;

    @Schema(description = "Tên chi nhánh", example = "Chi nhánh Quận 1")
    @NotBlank(message = "Tên chi nhánh không được để trống")
    private String branchName;

    @Schema(description = "Địa chỉ chi nhánh", example = "123 Nguyễn Văn Cừ, Quận 5, TP.HCM")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Schema(description = "Số điện thoại chi nhánh", example = "0281234567")
    private String phoneNumber;
}
