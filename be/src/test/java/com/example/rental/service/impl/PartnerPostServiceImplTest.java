package com.example.rental.service.impl;

import com.example.rental.entity.Employees;
import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.repository.PartnerPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PartnerPostServiceImpl Tests")
class PartnerPostServiceImplTest {

    @Mock private PartnerPostRepository partnerPostRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private PartnerPostServiceImpl partnerPostService;

    @Test
    @DisplayName("Create Post - Should set initial status")
    void createPost_ShouldSetPendingPaymentStatus() {
        PartnerPost post = new PartnerPost();
        when(partnerPostRepository.save(any(PartnerPost.class))).thenAnswer(i -> i.getArgument(0));

        PartnerPost result = partnerPostService.createPost(post);

        assertThat(result.getStatus()).isEqualTo(PostApprovalStatus.PENDING_PAYMENT);
        verify(partnerPostRepository).save(post);
    }

    @Test
    @DisplayName("Approve Post - Success")
    void approvePost_Success() {
        PartnerPost post = new PartnerPost();
        Employees employee = new Employees();
        
        when(partnerPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(partnerPostRepository.save(any(PartnerPost.class))).thenAnswer(i -> i.getArgument(0));

        PartnerPost result = partnerPostService.approvePost(1L, 10L, PostApprovalStatus.APPROVED);

        assertThat(result.getStatus()).isEqualTo(PostApprovalStatus.APPROVED);
        assertThat(result.getApprovedBy()).isEqualTo(employee);
        verify(partnerPostRepository).save(post);
    }

    @Test
    @DisplayName("Approve Post - Throw Error when status is PENDING_APPROVAL")
    void approvePost_ThrowsExceptionOnInvalidStatus() {
        PartnerPost post = new PartnerPost();
        when(partnerPostRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> partnerPostService.approvePost(1L, 10L, PostApprovalStatus.PENDING_APPROVAL))
                .hasMessageContaining("Không thể duyệt về trạng thái PENDING");
    }

    @Test
    @DisplayName("Update Post - Resubmit Logic after Rejected")
    void updatePost_HandlesRejectedPost() {
        PartnerPost post = new PartnerPost();
        post.setStatus(PostApprovalStatus.REJECTED);
        post.setUpdateCount(0);

        when(partnerPostRepository.save(any(PartnerPost.class))).thenAnswer(i -> i.getArgument(0));

        PartnerPost result = partnerPostService.updatePost(post);

        assertThat(result.getUpdateCount()).isEqualTo(1);
        assertThat(result.isUpdatedAfterReject()).isTrue();
        assertThat(result.getStatus()).isEqualTo(PostApprovalStatus.PENDING_APPROVAL);
        verify(partnerPostRepository).save(post);
    }

    @Test
    @DisplayName("Delete Post - Soft Delete Success")
    void deletePost_Success() {
        PartnerPost post = new PartnerPost();
        when(partnerPostRepository.findById(1L)).thenReturn(Optional.of(post));

        partnerPostService.deletePost(1L);

        assertThat(post.isDeleted()).isTrue();
        verify(partnerPostRepository).save(post);
    }
}
