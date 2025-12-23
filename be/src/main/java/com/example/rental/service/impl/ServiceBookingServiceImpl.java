package com.example.rental.service.impl;

import com.example.rental.dto.booking.CreateCleaningBookingRequest;
import com.example.rental.dto.booking.CleaningBookingManagerRow;
import com.example.rental.dto.booking.ServiceBookingResponse;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import com.example.rental.security.Audited;
import com.example.rental.entity.AuditAction;
import com.example.rental.service.ServiceBookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceBookingServiceImpl implements ServiceBookingService {

    private static final LocalTime CLEANING_START = LocalTime.of(8, 0);
    private static final LocalTime CLEANING_END = LocalTime.of(11, 0);

    private final ContractRepository contractRepository;
    private final RentalServiceRepository rentalServiceRepository;
    private final ServiceBookingRepository serviceBookingRepository;
    private final EmployeeRepository employeeRepository;

    private boolean isTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a != null && "ROLE_TENANT".equals(a.getAuthority()));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }

    private void assertTenantOwnsContract(Contract contract) {
        if (!isTenant()) return;
        String username = currentUsername();
        if (username == null || contract == null || contract.getTenant() == null || contract.getTenant().getUsername() == null) {
            throw new RuntimeException("Không có quyền truy cập hợp đồng");
        }
        if (!contract.getTenant().getUsername().equalsIgnoreCase(username)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên hợp đồng này");
        }
    }

    private boolean isManagerOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String v = a != null ? a.getAuthority() : null;
            return "ROLE_MANAGER".equals(v) || "ROLE_ADMIN".equals(v);
        });
    }

    private String getMyBranchCode() {
        String username = currentUsername();
        if (username == null) {
            throw new org.springframework.security.access.AccessDeniedException("Không xác định người dùng");
        }
        Employees e = employeeRepository.findByUsername(username)
                .or(() -> employeeRepository.findByUsernameIgnoreCase(username))
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Không tìm thấy nhân viên"));
        if (e.getBranch() == null || e.getBranch().getBranchCode() == null) {
            throw new IllegalStateException("Nhân viên chưa được gán chi nhánh");
        }
        return e.getBranch().getBranchCode();
    }

    private CleaningBookingManagerRow toManagerRow(ServiceBooking b) {
        Contract c = b.getContract();
        Room r = c != null ? c.getRoom() : null;
        Tenant t = c != null ? c.getTenant() : null;
        return CleaningBookingManagerRow.builder()
                .id(b.getId())
                .contractId(c != null ? c.getId() : null)
                .contractCode(null)
                .roomCode(r != null ? r.getRoomCode() : null)
                .roomNumber(r != null ? r.getRoomNumber() : null)
                .tenantName(t != null ? t.getFullName() : null)
                .tenantUsername(t != null ? t.getUsername() : null)
                .bookingDate(b.getBookingDate())
                .startTime(b.getStartTime() != null ? b.getStartTime().toString() : null)
                .endTime(b.getEndTime() != null ? b.getEndTime().toString() : null)
                .status(b.getStatus() != null ? b.getStatus().name() : null)
                .cancelReason(b.getCancelReason())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private ServiceBookingResponse toResponse(ServiceBooking b) {
        return ServiceBookingResponse.builder()
                .id(b.getId())
                .contractId(b.getContract() != null ? b.getContract().getId() : null)
                .serviceId(b.getService() != null ? b.getService().getId() : null)
                .serviceName(b.getService() != null ? b.getService().getServiceName() : null)
                .bookingDate(b.getBookingDate())
                .startTime(b.getStartTime() != null ? b.getStartTime().toString() : null)
                .endTime(b.getEndTime() != null ? b.getEndTime().toString() : null)
                .status(b.getStatus() != null ? b.getStatus().name() : null)
                .createdAt(b.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.ADD_SERVICE, targetType = "SERVICE_BOOKING", description = "Đăng ký dịch vụ vệ sinh tuần")
    public ServiceBookingResponse createNextCleaningBooking(Long contractId, CreateCleaningBookingRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng"));
        assertTenantOwnsContract(contract);

        // Backward-compatible: older seed used "Vệ sinh chung cư".
        RentalServiceItem cleaning = rentalServiceRepository.findByServiceNameIgnoreCase("Vệ sinh")
            .or(() -> rentalServiceRepository.findByServiceNameIgnoreCase("Vệ sinh chung cư"))
            .orElseThrow(() -> new RuntimeException("Chưa cấu hình dịch vụ 'Vệ sinh'"));

        // Booking rule:
        // - If user books on Thu/Fri/Sat/Sun => schedule next Thursday (exclusive).
        // - If user books on Mon/Tue/Wed (before the upcoming Thursday) => schedule Thursday of the following week.
        LocalDate today = LocalDate.now();
        LocalDate computed = today.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
        if (today.getDayOfWeek() == DayOfWeek.MONDAY
                || today.getDayOfWeek() == DayOfWeek.TUESDAY
                || today.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
            computed = computed.plusWeeks(1);
        }

        LocalDate bookingDate = computed;
        if (request != null && request.getBookingDate() != null) {
            // Keep API deterministic: client must match the computed next booking date.
            if (!request.getBookingDate().equals(computed)) {
                throw new RuntimeException("Ngày vệ sinh không hợp lệ. Hệ thống chỉ cho phép đăng ký lịch vào: " + computed);
            }
            bookingDate = request.getBookingDate();
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày đăng ký không hợp lệ");
        }
        if (bookingDate.getDayOfWeek() != DayOfWeek.THURSDAY) {
            throw new RuntimeException("Dịch vụ vệ sinh chỉ hỗ trợ vào Thứ 5");
        }

        if (serviceBookingRepository.existsByContract_IdAndService_IdAndBookingDate(contractId, cleaning.getId(), bookingDate)) {
            throw new RuntimeException("Đã có lịch vệ sinh cho ngày này");
        }

        ServiceBooking booking = ServiceBooking.builder()
                .contract(contract)
                .service(cleaning)
                .bookingDate(bookingDate)
                .startTime(CLEANING_START)
                .endTime(CLEANING_END)
                .status(ServiceBookingStatus.BOOKED)
                .build();

        return toResponse(serviceBookingRepository.save(booking));
    }

    @Override
    public List<ServiceBookingResponse> listBookingsForContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng"));
        assertTenantOwnsContract(contract);

        return serviceBookingRepository.findByContract_Id(contractId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.UPDATE_CONTRACT, targetType = "SERVICE_BOOKING", description = "Xác nhận đã vệ sinh")
    public ServiceBookingResponse markCompleted(Long bookingId) {
        ServiceBooking booking = serviceBookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch"));
        booking.setStatus(ServiceBookingStatus.COMPLETED);
        return toResponse(serviceBookingRepository.save(booking));
    }

    @Override
    public List<CleaningBookingManagerRow> listCleaningBookingsMyBranch() {
        if (!isManagerOrAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Không có quyền truy cập");
        }
        String branchCode = getMyBranchCode();
        List<ServiceBooking> rows = serviceBookingRepository.findForBranchAndServiceNameFromDate(
                branchCode,
                "Vệ sinh",
                null,
                null
        );
        return rows.stream().map(this::toManagerRow).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.UPDATE_CONTRACT, targetType = "SERVICE_BOOKING", description = "Hủy lịch vệ sinh")
    public ServiceBookingResponse cancelBookingAsManager(Long bookingId, String reason) {
        if (!isManagerOrAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Không có quyền truy cập");
        }

        ServiceBooking booking = serviceBookingRepository.findByIdWithContractRoomTenantService(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch"));

        String myBranch = getMyBranchCode();
        String bookingBranch = booking.getContract() != null && booking.getContract().getRoom() != null
                ? booking.getContract().getRoom().getBranchCode()
                : null;
        if (bookingBranch == null || !bookingBranch.equalsIgnoreCase(myBranch)) {
            throw new org.springframework.security.access.AccessDeniedException("Không có quyền thao tác lịch của chi nhánh khác");
        }

        if (booking.getStatus() == ServiceBookingStatus.CANCELED) {
            return toResponse(booking);
        }
        if (booking.getStatus() == ServiceBookingStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hủy lịch đã hoàn thành");
        }

        String trimmed = reason != null ? reason.trim() : "";
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy");
        }
        if (trimmed.length() > 255) {
            trimmed = trimmed.substring(0, 255);
        }

        booking.setStatus(ServiceBookingStatus.CANCELED);
        booking.setCancelReason(trimmed);
        booking.setCanceledAt(LocalDateTime.now());
        booking.setCanceledBy(currentUsername());
        return toResponse(serviceBookingRepository.save(booking));
    }
}
