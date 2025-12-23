package com.example.rental.controller;

import com.example.rental.dto.branch.BranchRequest;
import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.rental.dto.ApiResponseDto;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(name = "Branch API", description = "Quản lý chi nhánh")
@SecurityRequirement(name = "Bearer Authentication") // ✅ để Swagger gửi JWT
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'MANAGER')") // nếu muốn USER cũng xem thì bỏ MANAGER
    @Operation(summary = "Lấy danh sách tất cả chi nhánh")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<BranchResponse>>> getAllBranches() {
        java.util.List<BranchResponse> data = branchService.getAllBranches();
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Branches fetched", data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'MANAGER')")
    @Operation(summary = "Lấy chi nhánh theo ID")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<BranchResponse>> getBranchById(@PathVariable Long id) {
        BranchResponse resp = branchService.getBranchById(id);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Branch fetched", resp));
    }

    @GetMapping("/code/{branchCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'MANAGER')")
    @Operation(summary = "Lấy chi nhánh theo mã code")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<BranchResponse>> getBranchByCode(@PathVariable String branchCode) {
        BranchResponse resp = branchService.getBranchByCode(branchCode);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Branch fetched", resp));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @Operation(summary = "Tạo chi nhánh mới")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<BranchResponse>> createBranch(@Valid @RequestBody BranchRequest request) {
        BranchResponse resp = branchService.createBranch(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(com.example.rental.dto.ApiResponseDto.success(201, "Branch created", resp));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @Operation(summary = "Cập nhật chi nhánh theo ID")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<BranchResponse>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request
    ) {
        BranchResponse resp = branchService.updateBranch(id, request);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Branch updated", resp));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @Operation(summary = "Xóa chi nhánh theo ID")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Branch deleted"));
    }
}
