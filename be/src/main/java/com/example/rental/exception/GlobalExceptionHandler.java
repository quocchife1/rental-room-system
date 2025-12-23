package com.example.rental.exception;

import com.example.rental.dto.ApiResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage() != null ? ex.getMessage() : "Không tìm thấy dữ liệu",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : "Yêu cầu không hợp lệ",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : "Trạng thái không hợp lệ",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String raw = null;
        try {
            raw = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        } catch (Exception ignored) {
            raw = ex.getMessage();
        }

        String msg = "Dữ liệu không hợp lệ";
        if (raw != null) {
            String lower = raw.toLowerCase();
            if (lower.contains("out of range") && lower.contains("amount")) {
                msg = "Số tiền vượt quá giới hạn hệ thống (tối đa 9,999,999,999.99)";
            }
        }

        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.BAD_REQUEST.value(),
                msg,
                raw,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.BAD_REQUEST.value(),
                "Yêu cầu không hợp lệ",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Thông tin đăng nhập không hợp lệ",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.FORBIDDEN.value(),
                "Không có quyền truy cập",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Thông tin đăng nhập không hợp lệ",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleExpiredJwtException(ExpiredJwtException ex, HttpServletRequest request) {
        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.UNAUTHORIZED.value(),
                "JWT token hết hạn",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Dữ liệu không hợp lệ");

        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu không hợp lệ",
                errorMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        // Ensure root cause is visible in backend logs
        try {
            ex.printStackTrace();
        } catch (Exception ignored) {
            // ignore
        }

        String detail = ex.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = ex.getClass().getName();
        }

        ApiResponseDto<Void> response = ApiResponseDto.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Lỗi hệ thống",
                detail,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
