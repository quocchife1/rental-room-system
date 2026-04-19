package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/branches")
@RequiredArgsConstructor
@Tag(name = "Public Branch API", description = "Lấy danh sách chi nhánh công khai")
public class PublicBranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "Lấy danh sách chi nhánh công khai")
    public ResponseEntity<ApiResponseDto<List<BranchResponse>>> getAllBranches() {
        List<BranchResponse> data = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponseDto.success(200, "Branches fetched", data));
    }
}