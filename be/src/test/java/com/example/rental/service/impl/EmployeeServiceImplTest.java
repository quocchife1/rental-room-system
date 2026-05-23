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
import com.example.rental.service.util.CodeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl Tests")
class EmployeeServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private BranchService branchService;
    @Mock private CodeGenerator codeGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    @DisplayName("Register New Employee - Success")
    void registerNewEmployee_Success() {
        EmployeeRegisterRequest request = new EmployeeRegisterRequest();
        request.setUsername("emp01");
        request.setPassword("pass123");
        request.setBranchCode("B01");

        Branch branch = new Branch();
        Employees employee = mock(Employees.class); 
        EmployeeResponse response = EmployeeResponse.builder().build();

        when(branchService.findByBranchCode("B01")).thenReturn(Optional.of(branch));
        when(employeeMapper.toEntity(request)).thenReturn(employee);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(codeGenerator.generateCode(eq("NV"), any())).thenReturn("NV01");
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        EmployeeResponse result = employeeService.registerNewEmployee(request);

        assertThat(result).isNotNull();
        verify(employeeRepository, times(2)).save(any(Employees.class));
        verify(employee).setEmployeeCode("NV01");
    }

    @Test
    @DisplayName("Register New Employee - Branch Not Found")
    void registerNewEmployee_BranchNotFound() {
        EmployeeRegisterRequest request = new EmployeeRegisterRequest();
        request.setBranchCode("INVALID");
        when(branchService.findByBranchCode("INVALID")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.registerNewEmployee(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Update Status - Success")
    void updateStatus_Success() {
        Employees emp = new Employees();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(emp)).thenReturn(emp);
        Employees result = employeeService.updateStatus(1L, UserStatus.ACTIVE); 
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(employeeRepository).save(emp);
    }
}