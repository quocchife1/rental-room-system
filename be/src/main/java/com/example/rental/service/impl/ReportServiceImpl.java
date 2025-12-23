package com.example.rental.service.impl;

import com.example.rental.dto.reports.FinancialReportSummaryDTO;
import com.example.rental.entity.Invoice;
import com.example.rental.entity.InvoiceStatus;
import com.example.rental.entity.Employees;
import com.example.rental.repository.InvoiceRepository;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final InvoiceRepository invoiceRepository;
        private final EmployeeRepository employeeRepository;

        private boolean hasRole(String roleName) {
                try {
                        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                        if (auth == null || auth.getAuthorities() == null) return false;
                        String expected = roleName != null && roleName.startsWith("ROLE_") ? roleName : ("ROLE_" + roleName);
                        return auth.getAuthorities().stream().anyMatch(a -> expected.equals(a.getAuthority()));
                } catch (Exception ignored) {
                        return false;
                }
        }

        private boolean isManagerAuthenticated() {
                return hasRole("MANAGER");
        }

        private String currentUsername() {
                try {
                        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                        return auth != null ? auth.getName() : null;
                } catch (Exception ignored) {
                        return null;
                }
        }

        private Long getMyBranchIdForEmployee() {
                String username = currentUsername();
                if (username == null) {
                        throw new AccessDeniedException("Không xác định người dùng");
                }
                Employees e = employeeRepository.findByUsername(username)
                                .or(() -> employeeRepository.findByUsernameIgnoreCase(username))
                                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy nhân viên"));
                if (e.getBranch() == null || e.getBranch().getId() == null) {
                        throw new IllegalStateException("Nhân viên chưa được gán chi nhánh");
                }
                return e.getBranch().getId();
        }

    @Override
    public FinancialReportSummaryDTO getSummary(LocalDate from, LocalDate to, Long branchId) {
                Long effectiveBranchId = branchId;
                if (isManagerAuthenticated()) {
                        effectiveBranchId = getMyBranchIdForEmployee();
                }
                List<Invoice> invoiced = invoiceRepository.findForReport(from, to, effectiveBranchId);
                List<Invoice> paid = invoiceRepository.findPaidForReport(from, to, effectiveBranchId);

        BigDecimal revenue = invoiced.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidTotal = paid.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstanding = invoiced.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.UNPAID || i.getStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinancialReportSummaryDTO.builder()
                .revenue(revenue)
                .paid(paidTotal)
                .outstanding(outstanding)
                .invoiceCount(invoiced.size())
                .build();
    }
}
