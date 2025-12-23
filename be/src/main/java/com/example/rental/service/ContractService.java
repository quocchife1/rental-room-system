package com.example.rental.service;

import com.example.rental.dto.contract.ContractCreateRequest;
import com.example.rental.dto.contract.DepositPaymentRequest;
import com.example.rental.dto.contract.ContractUpdateRequest;
import com.example.rental.entity.Contract;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ContractService {
    Contract createContract(ContractCreateRequest request) throws IOException;
    List<Contract> findAll();
    Optional<Contract> findById(Long id);
    Contract uploadSignedContract(Long id, MultipartFile file) throws IOException;
    Resource downloadContract(Long id) throws IOException;
    java.util.List<Contract> findByTenantId(Long tenantId);
    Page<Contract> findByTenantId(Long tenantId, Pageable pageable);

    // Staff: danh sách hợp đồng theo chi nhánh (ADMIN xem toàn bộ)
    Page<Contract> getMyBranchContracts(String status, String query, Pageable pageable);

    // Staff: xem chi tiết hợp đồng (ADMIN xem toàn bộ; role khác theo chi nhánh)
    Contract getContractForStaff(Long id);

    // Staff: cập nhật hợp đồng khi còn PENDING
    Contract updateContractForStaff(Long id, ContractUpdateRequest request) throws IOException;

    // Staff: xóa hợp đồng tạm (PENDING)
    void deletePendingContractForStaff(Long id);

    // Staff: xác nhận thanh toán tiền cọc (CASH/BANK_TRANSFER) và kích hoạt hợp đồng
    Contract confirmDepositPaymentForStaff(Long id, DepositPaymentRequest request) throws IOException;
}
