package com.example.rental.mapper;

import com.example.rental.dto.branch.BranchRequest;
import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BranchMapper {

 
    BranchResponse toResponse(Branch branch);

    Branch toEntity(BranchRequest request);
    void updateEntityFromRequest(BranchRequest request, @MappingTarget Branch branch);
}