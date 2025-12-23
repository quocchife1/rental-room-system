package com.example.rental.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    
    // Thông tin User trả về ngay khi login
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address; // Có thể null với Employee/Guest
    private String role;    // TENANT, PARTNER, EMPLOYEE, GUEST
}