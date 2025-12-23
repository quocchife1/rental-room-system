package com.example.rental.service;

import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.PostApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface PartnerPostService {
    // Đối tác tạo tin đăng mới (Trạng thái ban đầu là PENDING)
    PartnerPost createPost(PartnerPost post);

    // Lấy tin đăng theo ID
    Optional<PartnerPost> findById(Long id);

    // Lấy tin đăng theo ID đối tác
    List<PartnerPost> findPostsByPartnerId(Long partnerId);

    // Lấy tin đăng theo ID đối tác (phân trang)
    Page<PartnerPost> findPostsByPartnerId(Long partnerId, Pageable pageable);

    // Lấy tin đăng theo trạng thái (Dành cho Admin duyệt)
    List<PartnerPost> findPostsByStatus(PostApprovalStatus status);

    // Public: Lấy tin đã duyệt/đang hiển thị (APPROVED/ACTIVE) với phân trang
    Page<PartnerPost> findPublicVisiblePosts(Pageable pageable);

    // Nhân viên duyệt tin (APPROVED/REJECTED)
    PartnerPost approvePost(Long postId, Long approvedByEmployeeId, PostApprovalStatus newStatus);

    // Đối tác cập nhật tin đăng (reset về PENDING_APPROVAL)
    PartnerPost updatePost(PartnerPost post);

    // Lưu trực tiếp không thay đổi status (dùng cho increment views)
    PartnerPost savePost(PartnerPost post);

    // Đối tác xóa tin đăng
    void deletePost(Long id);
}