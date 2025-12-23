package com.example.rental.service;

import com.example.rental.dto.branch.BranchRequest;
import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.entity.Branch;

import java.util.List;
import java.util.Optional;

public interface BranchService {
    // ===== ENTITY LAYER (dùng nội bộ) =====
    Optional<Branch> findById(Long id);
    Optional<Branch> findByBranchCode(String branchCode);

    // ===== DTO LAYER (dùng cho Controller) =====
    List<BranchResponse> getAllBranches();
    BranchResponse getBranchById(Long id);
    BranchResponse getBranchByCode(String branchCode);
    BranchResponse createBranch(BranchRequest request);
    BranchResponse updateBranch(Long id, BranchRequest request);
    void deleteBranch(Long id);
}
