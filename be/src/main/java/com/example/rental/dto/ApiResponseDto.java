package com.example.rental.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    @Schema(description = "Mã trạng thái HTTP hoặc mã ứng dụng", example = "200")
    private int statusCode;

    @Schema(description = "Thông điệp phản hồi", example = "Thành công")
    private String message;

    @Schema(description = "Chi tiết lỗi (nếu có)", example = "Dữ liệu không hợp lệ")
    private String error;

    @Schema(description = "Dữ liệu trả về từ API (nếu có)")
    private T data;

    @Schema(description = "Thời điểm phản hồi", example = "2025-10-07T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "Đường dẫn endpoint được gọi", example = "/api/files/upload/contract")
    private String path;

    // ✅ Trả về phản hồi thành công (không có dữ liệu)
    public static <T> ApiResponseDto<T> success(int statusCode, String message) {
        return new ApiResponseDto<>(statusCode, message, null, null, LocalDateTime.now(), null);
    }

    // ✅ Trả về phản hồi thành công (có dữ liệu)
    public static <T> ApiResponseDto<T> success(int statusCode, String message, T data) {
        return new ApiResponseDto<>(statusCode, message, null, data, LocalDateTime.now(), null);
    }

    // ✅ Trả về phản hồi lỗi chi tiết
    public static <T> ApiResponseDto<T> error(int statusCode, String message, String error, String path) {
        return new ApiResponseDto<>(statusCode, message, error, null, LocalDateTime.now(), path);
    }

    // ✅ Trả về phản hồi lỗi không có data
    public static <T> ApiResponseDto<T> error(int statusCode, String message, T data) {
        return new ApiResponseDto<>(statusCode, message, message, data, LocalDateTime.now(), null);
    }
}
