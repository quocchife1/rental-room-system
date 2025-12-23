package com.example.rental.service.impl;

import com.example.rental.dto.auth.EmployeeRegisterRequest;
import com.example.rental.dto.employee.EmployeeResponse;
import com.example.rental.entity.Branch;
import com.example.rental.entity.Employees;
import com.example.rental.entity.UserStatus;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.EmployeeMapper;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.service.BranchService;
import com.example.rental.service.EmployeeService;
import com.example.rental.service.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BranchService branchService;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse registerNewEmployee(EmployeeRegisterRequest request) {
        log.info("Registering new employee username='{}' branchCode='{}'", request.getUsername(), request.getBranchCode());

        Branch branch = branchService.findByBranchCode(request.getBranchCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "code", request.getBranchCode()
                ));

        Employees newEmployee = employeeMapper.toEntity(request);
        newEmployee.setPassword(passwordEncoder.encode(request.getPassword()));
        newEmployee.setBranch(branch);
        newEmployee.setStatus(UserStatus.ACTIVE);

        Employees savedEmployee = employeeRepository.save(newEmployee);

        String employeeCode = codeGenerator.generateCode("NV", savedEmployee.getId());
        savedEmployee.setEmployeeCode(employeeCode);

        Employees finalEmployee = employeeRepository.save(savedEmployee);
        return employeeMapper.toResponse(finalEmployee);
    }

    @Override
    public Optional<Employees> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Optional<Employees> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    @Override
    public List<Employees> findAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Page<com.example.rental.dto.employee.EmployeeResponse> findAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
    }

    @Override
    @Transactional
    public Employees updateStatus(Long employeeId, UserStatus status) {
        Employees employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        employee.setStatus(status);
        return employeeRepository.save(employee);
    }
}
