package com.example.rental.mapper;

import com.example.rental.dto.auth.EmployeeRegisterRequest;
import com.example.rental.dto.employee.EmployeeResponse;
import com.example.rental.entity.Employees;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {BranchMapper.class})
public interface EmployeeMapper {

    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    EmployeeResponse toResponse(Employees employee);

    List<EmployeeResponse> toResponseList(List<Employees> employees);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "phone", target = "phoneNumber")
    Employees toEntity(EmployeeRegisterRequest request);
}
