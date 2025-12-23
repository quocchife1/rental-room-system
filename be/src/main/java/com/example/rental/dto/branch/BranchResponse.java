package com.example.rental.dto.branch;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BranchResponse {
    private Long id;
    private String branchCode;
    private String branchName;
    private String address;
    private String phoneNumber;
}