package com.example.rental.service;

import com.example.rental.dto.auth.AuthLoginRequest;
import com.example.rental.dto.auth.AuthRegisterRequest;
import com.example.rental.dto.auth.AuthResponse;
import com.example.rental.dto.auth.EmployeeRegisterRequest;
import com.example.rental.dto.auth.PartnerRegisterRequest;

public interface AuthService {
    void registerGuest(AuthRegisterRequest request);
    void registerTenant(AuthRegisterRequest request);
    void registerPartner(PartnerRegisterRequest request);
    void registerEmployee(EmployeeRegisterRequest request);
    
    AuthResponse login(AuthLoginRequest request);
}