package com.example.rental.service;

import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.entity.DamageReport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface DamageReportService {

    /**
     * Tạo báo cáo hư hỏng mới (DRAFT)
     */
    DamageReportResponse createDamageReport(DamageReportCreateRequest request, List<MultipartFile> images)
            throws IOException;

    /**
     * Lấy báo cáo hư hỏng theo ID
     */
    Optional<DamageReport> findById(Long id);

    DamageReportResponse getById(Long id);

    /**
     * Lấy tất cả báo cáo hư hỏng của một hợp đồng
     */
    List<DamageReportResponse> getByContractId(Long contractId);

    /**
     * Lấy tất cả báo cáo hư hỏng theo trạng thái
     */
    List<DamageReportResponse> getByStatus(String status);

    /**
     * Lấy tất cả báo cáo hư hỏng
     */
    List<DamageReportResponse> getAll();

    /**
     * Cập nhật báo cáo hư hỏng (DRAFT -> SUBMITTED)
     */
    DamageReportResponse updateDamageReport(Long id, DamageReportCreateRequest request, List<MultipartFile> newImages)
            throws IOException;

    /**
     * Gửi báo cáo để duyệt (SUBMITTED)
     */
    DamageReportResponse submitForApproval(Long id);

    /**
     * Phê duyệt báo cáo (APPROVED)
     */
    DamageReportResponse approveDamageReport(Long id, String approverNote);

    /**
     * Từ chối báo cáo (REJECTED)
     */
    DamageReportResponse rejectDamageReport(Long id, String rejectReason);

    /**
     * Xóa báo cáo (chỉ ở trạng thái DRAFT)
     */
    void deleteDamageReport(Long id);

    /**
     * Tải hình ảnh từ báo cáo
     */
    String uploadDamageImages(Long damageReportId, List<MultipartFile> images) throws IOException;
}
