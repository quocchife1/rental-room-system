package com.example.rental.controller;

import com.example.rental.dto.employee.EmployeeResponse;
import com.example.rental.entity.Employees;
import com.example.rental.mapper.EmployeeMapper;
import com.example.rental.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.Mockito.when;

@DisplayName("EmployeeSelfController tests")
class EmployeeSelfControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private EmployeeMapper employeeMapper;

    @Test
    @WithMockUser(username = "emp1", roles = "MANAGER")
    @DisplayName("GET /api/employees/me -> 200")
    void me_shouldReturn200() throws Exception {
        Employees emp = new Employees();
        emp.setId(5L);
        emp.setUsername("emp1");

        EmployeeResponse resp = EmployeeResponse.builder().id(5L).username("emp1").build();

        when(employeeService.findByUsername("emp1")).thenReturn(Optional.of(emp));
        when(employeeMapper.toResponse(emp)).thenReturn(resp);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/me"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("emp1"));
    }

    @Test
    @WithMockUser(username = "emp1", roles = "MANAGER")
    @DisplayName("GET /api/employees/me -> 404 when not found")
    void me_notFound_shouldReturn404() throws Exception {
        when(employeeService.findByUsername("emp1")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/me"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(404));
    }
}
