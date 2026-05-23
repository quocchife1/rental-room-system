package com.example.rental.service.impl;

import com.example.rental.dto.audit.AuditLogDTO;
import com.example.rental.dto.audit.AuditLogSearchCriteria;
import com.example.rental.entity.AuditAction;
import com.example.rental.entity.AuditLog;
import com.example.rental.mapper.AuditLogMapper;
import com.example.rental.repository.AuditLogRepository;
import com.example.rental.service.AuditStatistics;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogServiceImpl Tests")
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void logAction_success() {
        AuditLog saved = new AuditLog();
        saved.setId(1L);
        saved.setActorId("admin");
        saved.setAction(AuditAction.CREATE_CONTRACT);

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(saved);

        AuditLog result = auditLogService.logAction("admin", "ROLE_ADMIN", AuditAction.CREATE_CONTRACT,
                "CONTRACT", 1L, "Created contract", "old", "new", "127.0.0.1", 1L, "Chrome", "SUCCESS", null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void logAction_truncateLongValues() {
        String longText = "a".repeat(2500);
        AuditLog saved = new AuditLog();
        saved.setId(1L);

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog arg = invocation.getArgument(0);
            assertThat(arg.getOldValue().length()).isEqualTo(2000);
            assertThat(arg.getNewValue().length()).isEqualTo(2000);
            return arg;
        });

        auditLogService.logAction("admin", "ROLE_ADMIN", AuditAction.CREATE_CONTRACT,
                "CONTRACT", 1L, "Created contract", longText, longText, "127.0.0.1", 1L, "Chrome", "SUCCESS", null);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void logAction_retryOnFailure() {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("DB error"))
                .thenReturn(new AuditLog());

        AuditLog result = auditLogService.logAction("admin", "ROLE_ADMIN", AuditAction.CREATE_CONTRACT,
                "CONTRACT", 1L, "desc", null, null, null, null, null, null, null);

        assertThat(result).isNotNull();
        verify(auditLogRepository, times(2)).save(any(AuditLog.class));
    }

    @Test
    void logAction_failAfterRetries() {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("DB error"));

        AuditLog result = auditLogService.logAction("admin", "ROLE_ADMIN", AuditAction.CREATE_CONTRACT,
                "CONTRACT", 1L, "desc", null, null, null, null, null, null, null);

        assertThat(result).isNull();
        verify(auditLogRepository, times(2)).save(any(AuditLog.class));
    }

    @Test
    void logAction_interruptedDuringSleep() {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("DB error"));

        // Cố tình interrupt thread hiện tại để kích hoạt catch (InterruptedException)
        Thread.currentThread().interrupt();

        AuditLog result = auditLogService.logAction("admin", "ROLE_ADMIN", AuditAction.CREATE_CONTRACT,
                "CONTRACT", 1L, "desc", null, null, null, null, null, null, null);

        assertThat(result).isNull();
        // Xóa cờ interrupt để không ảnh hưởng test khác
        Thread.interrupted(); 
    }

    @Test
    void getAuditTrail_shouldReturnList() {
        List<AuditLog> list = List.of(new AuditLog());
        when(auditLogRepository.findByTargetTypeAndId("CONTRACT", 1L)).thenReturn(list);
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        List<AuditLogDTO> res = auditLogService.getAuditTrail("CONTRACT", 1L);

        assertThat(res).hasSize(1);
    }

    @Test
    void getAuditTrailPaged_shouldReturnPage() {
        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));
        when(auditLogRepository.findByTargetTypeAndIdPaged(eq("CONTRACT"), eq(1L), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        Page<AuditLogDTO> res = auditLogService.getAuditTrailPaged("CONTRACT", 1L, PageRequest.of(0, 10));

        assertThat(res.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getByAction_shouldReturnPage() {
        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));
        when(auditLogRepository.findByAction(eq(AuditAction.CREATE_CONTRACT), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        Page<AuditLogDTO> res = auditLogService.getByAction(AuditAction.CREATE_CONTRACT, PageRequest.of(0, 10));

        assertThat(res.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getByActorId_shouldReturnPage() {
        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));
        when(auditLogRepository.findByActorId(eq("admin"), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        Page<AuditLogDTO> res = auditLogService.getByActorId("admin", PageRequest.of(0, 10));

        assertThat(res.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getByDateRange_shouldReturnPage() {
        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        when(auditLogRepository.findByDateRange(eq(start), eq(end), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        Page<AuditLogDTO> res = auditLogService.getByDateRange(start, end, PageRequest.of(0, 10));

        assertThat(res.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getByBranch_shouldReturnPage() {
        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));
        when(auditLogRepository.findByBranchId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        Page<AuditLogDTO> res = auditLogService.getByBranch(1L, PageRequest.of(0, 10));

        assertThat(res.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getPaymentHistory_shouldFilterConfirmPayment() {
        AuditLog l1 = new AuditLog();
        l1.setAction(AuditAction.CONFIRM_PAYMENT);
        AuditLog l2 = new AuditLog();
        l2.setAction(AuditAction.CREATE_CONTRACT);
        
        when(auditLogRepository.findByTargetTypeAndId("INVOICE", 1L)).thenReturn(List.of(l1, l2));
        when(auditLogMapper.toDTO(l1)).thenReturn(new AuditLogDTO());

        List<AuditLogDTO> res = auditLogService.getPaymentHistory(1L);

        assertThat(res).hasSize(1);
    }

    @Test
    void getConfirmedPayments_shouldReturnList() {
        when(auditLogRepository.findConfirmedPayments(any(), any())).thenReturn(List.of(new AuditLog()));
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        List<AuditLogDTO> res = auditLogService.getConfirmedPayments(LocalDateTime.now(), LocalDateTime.now());

        assertThat(res).hasSize(1);
    }

    @Test
    void getContractAuditTrail_shouldReturnList() {
        when(auditLogRepository.findContractChanges(1L)).thenReturn(List.of(new AuditLog()));
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        List<AuditLogDTO> res = auditLogService.getContractAuditTrail(1L);

        assertThat(res).hasSize(1);
    }

    @Test
    void getById_success() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(new AuditLog()));
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        AuditLogDTO res = auditLogService.getById(1L);

        assertThat(res).isNotNull();
    }

    @Test
    void getById_notFound() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.getById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Audit log not found");
    }

    @Test
    void getAll_shouldReturnPage() {
        Page<AuditLog> page = new PageImpl<>(List.of(new AuditLog()));
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        Page<AuditLogDTO> res = auditLogService.getAll(PageRequest.of(0, 10));

        assertThat(res.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getStatistics_shouldReturnStats() {
        AuditLog l1 = new AuditLog(); l1.setCreatedAt(LocalDateTime.now()); l1.setAction(AuditAction.CREATE_CONTRACT); l1.setTargetType("CONTRACT"); l1.setActorId("admin");
        AuditLog l2 = new AuditLog(); l2.setCreatedAt(LocalDateTime.now()); l2.setAction(AuditAction.UPDATE_CONTRACT); l2.setTargetType("INVOICE"); l2.setActorId("admin");
        AuditLog l3 = new AuditLog(); l3.setCreatedAt(LocalDateTime.now()); l3.setAction(AuditAction.DELETE_DATA); l3.setTargetType("TENANT"); l3.setActorId("user");
        AuditLog l4 = new AuditLog(); l4.setCreatedAt(LocalDateTime.now()); l4.setAction(AuditAction.APPROVE_PARTNER_POST); l4.setTargetType("POST"); l4.setActorId("manager");
        AuditLog l5 = new AuditLog(); l5.setCreatedAt(LocalDateTime.now()); l5.setAction(AuditAction.REJECT_PARTNER_POST); l5.setTargetType("POST"); l5.setActorId("manager");
        AuditLog l6 = new AuditLog(); l6.setCreatedAt(LocalDateTime.now()); l6.setAction(AuditAction.CONFIRM_PAYMENT); l6.setTargetType("PAYMENT"); l6.setActorId("admin");

        Page<AuditLog> page = new PageImpl<>(List.of(l1, l2, l3, l4, l5, l6));
        when(auditLogRepository.findByBranchId(eq(1L), any(Pageable.class))).thenReturn(page);

        AuditStatistics stats = auditLogService.getStatistics(
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 1L);

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalActions()).isEqualTo(6);
        assertThat(stats.getTotalActors()).isEqualTo(3); // admin, user, manager
        assertThat(stats.getTotalModifiedEntities()).isEqualTo(5); // CONTRACT, INVOICE, TENANT, POST, PAYMENT
        assertThat(stats.getCreateCount()).isEqualTo(1);
        assertThat(stats.getUpdateCount()).isEqualTo(1);
        assertThat(stats.getDeleteCount()).isEqualTo(1);
        assertThat(stats.getApproveCount()).isEqualTo(1);
        assertThat(stats.getRejectCount()).isEqualTo(1);
        assertThat(stats.getConfirmPaymentCount()).isEqualTo(1);
        assertThat(stats.getContractChanges()).isEqualTo(1);
        assertThat(stats.getInvoiceChanges()).isEqualTo(1);
        assertThat(stats.getTenantChanges()).isEqualTo(1);
    }

    // =========================================================================================
    // SECTION: MOCK JPA SPECIFICATION
    // Sử dụng ArgumentCaptor để lấy Specification Lambda và ép nó thực thi để phủ Coverage 100%
    // =========================================================================================
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void search_shouldExecuteSpecificationLambda_fullCriteria() {
        ArgumentCaptor<Specification<AuditLog>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(auditLogRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(Page.empty());

        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria();
        criteria.setFrom("2026-05-22T00:00:00"); // iso-local
        criteria.setTo("2026-05-22T23:59:59Z"); // iso-date-time
        criteria.setBranchId(1L);
        criteria.setActor("admin");
        criteria.setAction("CREATE_CONTRACT");
        criteria.setEntityType("CONTRACT");
        criteria.setEntityId(1L);

        auditLogService.search(criteria, PageRequest.of(0, 10));

        Specification<AuditLog> spec = specCaptor.getValue();

        // Chuẩn bị Mocks cho CriteriaBuilder
        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        
        Path pathMock = mock(Path.class);
        Expression exprMock = mock(Expression.class);
        when(root.get(anyString())).thenReturn(pathMock);
        when(cb.lower(any())).thenReturn(exprMock);
        when(cb.upper(any())).thenReturn(exprMock);

        // Kích hoạt đoạn code bên trong lambda Specification
        spec.toPredicate(root, query, cb);

        // Verify các logic bên trong thực sự đã được gọi
        verify(cb).between(any(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(cb).like(any(), eq("%admin%"));
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void search_shouldExecuteSpecificationLambda_partialCriteria_fromOnly_and_invalidDate() {
        ArgumentCaptor<Specification<AuditLog>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(auditLogRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(Page.empty());

        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria();
        criteria.setFrom("2026-05-22T00:00:00");
        criteria.setTo("Lỗi Parse Date Cố Tình"); // Phủ nhánh catch ParseException

        auditLogService.search(criteria, PageRequest.of(0, 10));

        Specification<AuditLog> spec = specCaptor.getValue();
        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        when(root.get(anyString())).thenReturn(mock(Path.class));

        spec.toPredicate(root, query, cb);

        verify(cb).greaterThanOrEqualTo(any(), any(LocalDateTime.class)); // Chỉ có From
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void search_shouldExecuteSpecificationLambda_partialCriteria_toOnly() {
        ArgumentCaptor<Specification<AuditLog>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(auditLogRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(Page.empty());

        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria();
        criteria.setTo("2026-05-22T23:59:59Z");

        auditLogService.search(criteria, PageRequest.of(0, 10));

        Specification<AuditLog> spec = specCaptor.getValue();
        Root<AuditLog> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        when(root.get(anyString())).thenReturn(mock(Path.class));

        spec.toPredicate(root, query, cb);

        verify(cb).lessThanOrEqualTo(any(), any(LocalDateTime.class)); // Chỉ có To
    }
}
