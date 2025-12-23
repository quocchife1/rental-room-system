package com.example.rental.repository;

import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.PostApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface PartnerPostRepository extends JpaRepository<PartnerPost, Long> {
    // Tìm bài đăng theo ID đối tác (loại bỏ bản ghi đã xóa)
    List<PartnerPost> findByPartnerIdAndIsDeletedFalse(Long partnerId);

    // Phân trang bài đăng theo ID đối tác (loại bỏ bản ghi đã xóa)
    Page<PartnerPost> findByPartnerIdAndIsDeletedFalse(Long partnerId, Pageable pageable);

    // Tìm bài đăng theo trạng thái duyệt (loại bỏ bản ghi đã xóa)
    List<PartnerPost> findByStatusAndIsDeletedFalse(PostApprovalStatus status);

    // Phân trang theo tập trạng thái (ví dụ APPROVED/ACTIVE), loại bỏ bản ghi đã
    // xóa
    Page<PartnerPost> findByStatusInAndIsDeletedFalse(List<PostApprovalStatus> statuses, Pageable pageable);

    // Phân trang theo tập trạng thái + tìm theo tiêu đề (contains, ignore case),
    // loại bỏ bản ghi đã xóa
    Page<PartnerPost> findByStatusInAndTitleContainingIgnoreCaseAndIsDeletedFalse(List<PostApprovalStatus> statuses,
            String title, Pageable pageable);

    Optional<PartnerPost> findByOrderId(String orderId);

    // Query with JOIN FETCH to eagerly load partner and approvedBy
    @Query(value = "SELECT p FROM PartnerPost p LEFT JOIN FETCH p.partner LEFT JOIN FETCH p.approvedBy WHERE p.status IN :statuses AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) AND p.isDeleted = false", countQuery = "SELECT COUNT(p) FROM PartnerPost p WHERE p.status IN :statuses AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) AND p.isDeleted = false")
    Page<PartnerPost> findWithPartnerAndApprovedBy(@Param("statuses") List<PostApprovalStatus> statuses,
            @Param("title") String title, Pageable pageable);

    // Projection-based paging for moderation list
    @Query(value = "SELECT new com.example.rental.dto.partnerpost.PartnerPostListItem(p.id, p.title, p.description, p.price, p.area, p.address, p.postType, p.status, p.createdAt, p.approvedAt, (CASE WHEN ab.fullName IS NOT NULL THEN ab.fullName ELSE NULL END), pr.id, pr.companyName, pr.phoneNumber, p.rejectReason) "
            +
            "FROM PartnerPost p LEFT JOIN p.approvedBy ab LEFT JOIN p.partner pr " +
            "WHERE p.isDeleted = false AND p.status IN :statuses AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))", countQuery = "SELECT COUNT(p) FROM PartnerPost p WHERE p.isDeleted = false AND p.status IN :statuses AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<com.example.rental.dto.partnerpost.PartnerPostListItem> pageListItems(
            @Param("statuses") List<PostApprovalStatus> statuses, @Param("title") String title, Pageable pageable);

    // Counters for stats
    @Query("SELECT COUNT(p) FROM PartnerPost p WHERE p.status = :status AND p.isDeleted = false")
    long countByStatusAndIsDeletedFalse(@Param("status") PostApprovalStatus status);

    @Query("SELECT COUNT(p) FROM PartnerPost p WHERE p.status IN :statuses AND p.isDeleted = false")
    long countByStatusInAndIsDeletedFalse(@Param("statuses") List<PostApprovalStatus> statuses);

    @Query("SELECT COUNT(p) FROM PartnerPost p WHERE p.approvedAt BETWEEN :start AND :end AND p.isDeleted = false")
    long countByApprovedAtBetweenAndIsDeletedFalse(@Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);
}