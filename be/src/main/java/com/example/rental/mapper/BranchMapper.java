package com.example.rental.mapper;

import com.example.rental.dto.branch.BranchRequest;
import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    // Entity → Response DTO
    BranchResponse toResponse(Branch branch);

    // Request DTO → Entity
    Branch toEntity(BranchRequest request);

    // Cập nhật Entity từ Request
    void updateEntityFromRequest(BranchRequest request, @MappingTarget Branch branch);
}
