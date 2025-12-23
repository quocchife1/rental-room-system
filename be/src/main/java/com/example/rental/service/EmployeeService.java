package com.example.rental.service;

import com.example.rental.dto.auth.EmployeeRegisterRequest;
import com.example.rental.dto.employee.EmployeeResponse;
import com.example.rental.entity.Employees;
import com.example.rental.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    EmployeeResponse registerNewEmployee(EmployeeRegisterRequest request);

    Optional<Employees> findById(Long id);
    Optional<Employees> findByUsername(String username);
    List<Employees> findAllEmployees();
    Page<EmployeeResponse> findAllEmployees(Pageable pageable);
    Employees updateStatus(Long employeeId, UserStatus status);
}