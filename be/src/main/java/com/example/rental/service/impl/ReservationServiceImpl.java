package com.example.rental.service.impl;

import com.example.rental.dto.reservation.ReservationRequest;
import com.example.rental.dto.reservation.ReservationResponse;
import com.example.rental.dto.contract.ContractPrefillResponse;
import com.example.rental.entity.*;
import com.example.rental.mapper.ReservationMapper;
import com.example.rental.repository.*; // Import wildcard để lấy hết repository
import com.example.rental.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.rental.security.Audited;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final ContractRepository contractRepository;
    private final ReservationMapper reservationMapper;
    
    // Đã thêm import repository.* ở trên nên dòng này sẽ hoạt động
    private final GuestRepository guestRepository; 

    private final EmployeeRepository employeeRepository;

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        String wanted1 = "ROLE_" + role;
        String wanted2 = role;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (a == null || a.getAuthority() == null) continue;
            String v = a.getAuthority();
            if (wanted1.equalsIgnoreCase(v) || wanted2.equalsIgnoreCase(v)) return true;
        }
        return false;
    }

    private String getCurrentEmployeeBranchCode() {
        String username = getCurrentUsername();
        if (username == null) throw new UsernameNotFoundException("Unauthenticated");

        Employees emp = employeeRepository.findByUsername(username)
                .or(() -> employeeRepository.findByUsernameIgnoreCase(username))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên."));

        if (emp.getBranch() == null || emp.getBranch().getBranchCode() == null) {
            throw new RuntimeException("Nhân viên chưa được gán chi nhánh.");
        }

        return emp.getBranch().getBranchCode();
    }

    private List<LocalDate> computeAllowedVisitDates(LocalDate today) {
        Set<LocalDate> unique = new HashSet<>();
        for (int offset = 1; offset <= 3; offset++) {
            LocalDate date = today.plusDays(offset);
            DayOfWeek dow = date.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY) {
                date = date.plusDays(2);
            } else if (dow == DayOfWeek.SUNDAY) {
                date = date.plusDays(1);
            }
            unique.add(date);
        }
        List<LocalDate> sorted = new ArrayList<>(unique);
        sorted.sort(LocalDate::compareTo);
        return sorted;
    }

    private void validateVisitDate(LocalDate visitDate) {
        if (visitDate == null) {
            throw new RuntimeException("Vui lòng chọn ngày đến tham khảo.");
        }
        LocalDate today = LocalDate.now();
        if (!visitDate.isAfter(today)) {
            throw new RuntimeException("Ngày đến tham khảo phải là ngày sau hôm nay.");
        }
        List<LocalDate> allowed = computeAllowedVisitDates(today);
        if (!allowed.contains(visitDate)) {
            String allowedStr = allowed.stream()
                    .map(d -> d.toString())
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Ngày tham khảo không hợp lệ. Chỉ được chọn một trong các ngày: " + allowedStr);
        }
    }

    // --- Create Methods ---

    @Override
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

@Override
    @Transactional
    @Audited(action = AuditAction.CREATE_RESERVATION, targetType = "RESERVATION", description = "Tạo yêu cầu giữ phòng")
    public ReservationResponse createReservation(ReservationRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RuntimeException("Phòng này hiện không có sẵn (Đã thuê hoặc đang bảo trì).");
        }

        validateVisitDate(request.getVisitDate());

        String username = getCurrentUsername();
        if (username == null) {
            throw new UsernameNotFoundException("Người dùng chưa xác thực");
        }
        
        // Logic tìm Tenant (đã có hoặc tạo mới từ Guest)
        Tenant tenant = tenantRepository.findByUsername(username)
            .or(() -> tenantRepository.findByUsernameIgnoreCase(username))
            .orElseGet(() -> {
                // Nếu chưa có Tenant -> Tìm Guest và tạo Tenant mới
                Guest guest = guestRepository.findByUsername(username)
                        .or(() -> guestRepository.findByUsernameIgnoreCase(username))
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản người dùng."));
                
                Tenant newTenant = new Tenant();
                newTenant.setUsername(guest.getUsername());
                newTenant.setPassword(guest.getPassword());
                newTenant.setEmail(guest.getEmail());
                newTenant.setFullName(guest.getFullName());
                newTenant.setPhoneNumber(guest.getPhoneNumber());
                newTenant.setStatus(UserStatus.ACTIVE);
                newTenant.setAddress("Cập nhật sau"); 
                
                log.info("Creating new Tenant profile for Guest: {}", username);
                return tenantRepository.save(newTenant);
            });

        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setRoom(room);
        reservation.setTenant(tenant);
        reservation.setReservationCode("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING_CONFIRMATION);
        reservation.setExpirationDate(LocalDateTime.now().plusDays(2)); 

        Reservation savedReservation = reservationRepository.save(reservation);
        
        // Cập nhật trạng thái phòng -> RESERVED
        room.setStatus(RoomStatus.RESERVED);
        roomRepository.save(room);
        
        return reservationMapper.toResponse(savedReservation);
    }
    // --- Read Methods ---

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    @Override
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giữ phòng với ID: " + id));
        return reservationMapper.toResponse(reservation);
    }

    @Override
    public List<Reservation> findReservationsByTenantId(Long tenantId) {
        return reservationRepository.findByTenantId(tenantId);
    }

    @Override
    public Page<ReservationResponse> getReservationsByTenant(Long tenantId, Pageable pageable) {
        Page<Reservation> page = reservationRepository.findByTenantId(tenantId, pageable);
        return page.map(reservationMapper::toResponse);
    }

@Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getMyReservations(Pageable pageable) {
        String username = getCurrentUsername();
        if (username == null) throw new UsernameNotFoundException("Unauthenticated");
        
        log.info("Fetching reservations for username: {}", username);

        // Tìm Tenant ID dựa trên username
        Optional<Tenant> tenantOpt = tenantRepository.findByUsername(username)
                .or(() -> tenantRepository.findByUsernameIgnoreCase(username));
        
        if (tenantOpt.isEmpty()) {
            log.warn("User {} has no Tenant profile yet. Returning empty list.", username);
            return Page.empty(pageable);
        }

        Long tenantId = tenantOpt.get().getId();
        log.info("Found Tenant ID: {}", tenantId);

        // Truy vấn DB
        return reservationRepository.findByTenantId(tenantId, pageable)
                .map(reservationMapper::toResponse);
    }    @Override
    public List<Reservation> findReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    @Override
    public Page<ReservationResponse> getReservationsByStatus(String statusStr, Pageable pageable) {
        try {
            ReservationStatus status = ReservationStatus.valueOf(statusStr.toUpperCase());
            Page<Reservation> page = reservationRepository.findByStatus(status, pageable);
            return page.map(reservationMapper::toResponse);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + statusStr);
        }
    }

    @Override
    public List<ReservationResponse> getReservationsByRoom(Long roomId) {
        List<Reservation> list = reservationRepository.findByRoomId(roomId);
        return list.stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- Action Methods ---

    @Override
    @Transactional
    @Audited(action = AuditAction.CONFIRM_RESERVATION, targetType = "RESERVATION", description = "Xác nhận giữ phòng")
    public ReservationResponse confirmReservation(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId).orElseThrow();
        r.setStatus(ReservationStatus.RESERVED);
        Room room = r.getRoom();
        room.setStatus(RoomStatus.RESERVED);
        roomRepository.save(room);
        return reservationMapper.toResponse(reservationRepository.save(r));
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CANCEL_RESERVATION, targetType = "RESERVATION", description = "Huỷ giữ phòng")
    public Reservation cancelReservation(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId).orElseThrow();
        Room room = r.getRoom();
        if (room != null && room.getStatus() == RoomStatus.RESERVED) {
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }
        r.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(r);
    }

    @Override
    @Transactional
    public ReservationResponse markCompleted(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giữ phòng."));

        // Sau khi xử lý xong lịch tham khảo, trả phòng về AVAILABLE nếu đang bị giữ
        Room room = r.getRoom();
        if (room != null && room.getStatus() == RoomStatus.RESERVED) {
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }

        r.setStatus(ReservationStatus.COMPLETED);
        return reservationMapper.toResponse(reservationRepository.save(r));
    }

    @Override
    @Transactional
    public ReservationResponse markNoShow(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giữ phòng."));

        Room room = r.getRoom();
        if (room != null && room.getStatus() == RoomStatus.RESERVED) {
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }

        r.setStatus(ReservationStatus.NO_SHOW);
        return reservationMapper.toResponse(reservationRepository.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> searchReservations(String query, Pageable pageable) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            return Page.empty(pageable);
        }
        return reservationRepository.search(q, pageable).map(reservationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getMyBranchReservations(String status, String query, Pageable pageable) {
        String q = query == null ? "" : query.trim();
        String statusStr = status == null ? "" : status.trim();

        ReservationStatus statusEnum = null;
        if (!statusStr.isEmpty() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                statusEnum = ReservationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Trạng thái không hợp lệ: " + statusStr);
            }
        }

        // ADMIN: xem toàn bộ, không giới hạn chi nhánh
        if (hasRole("ADMIN")) {
            if (!q.isEmpty()) {
                return reservationRepository.searchGlobal(q, statusEnum, pageable)
                        .map(reservationMapper::toResponse);
            }
            if (statusEnum != null) {
                return reservationRepository.findByStatus(statusEnum, pageable)
                        .map(reservationMapper::toResponse);
            }
            return reservationRepository.findAll(pageable)
                    .map(reservationMapper::toResponse);
        }

        // MANAGER/RECEPTIONIST: chỉ xem chi nhánh của mình
        String branchCode = getCurrentEmployeeBranchCode();

        if (!q.isEmpty()) {
            return reservationRepository.searchInBranch(branchCode, q, statusEnum, pageable)
                    .map(reservationMapper::toResponse);
        }

        if (statusEnum != null) {
            return reservationRepository.findByRoomBranchCodeAndStatus(branchCode, statusEnum, pageable)
                    .map(reservationMapper::toResponse);
        }

        return reservationRepository.findByRoomBranchCode(branchCode, pageable)
                .map(reservationMapper::toResponse);
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CREATE_CONTRACT, targetType = "CONTRACT", description = "Chuyển giữ phòng thành hợp đồng")
    public Long convertReservationToContract(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt phòng."));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new RuntimeException("Phiếu giữ phòng chưa được xác nhận.");
        }

        Contract contract = new Contract();
        contract.setTenant(reservation.getTenant());
        contract.setRoom(reservation.getRoom());
        
        if (reservation.getStartDate() != null) {
            contract.setStartDate(reservation.getStartDate().toLocalDate());
        } else {
            contract.setStartDate(java.time.LocalDate.now()); 
        }
        
        if (reservation.getEndDate() != null) {
            contract.setEndDate(reservation.getEndDate().toLocalDate());
        }

        Room room = reservation.getRoom();
        if (room != null) {
            if (room.getBranch() != null) {
                contract.setBranchCode(room.getBranch().getBranchCode());
            } else {
                contract.setBranchCode("DEFAULT");
            }
            contract.setRoomNumber(room.getRoomNumber());
            contract.setDeposit(room.getPrice());
        }

        contract.setStatus(ContractStatus.ACTIVE);
        Contract savedContract = contractRepository.save(contract);

        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
        
        if (room != null) {
            room.setStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }

        return savedContract.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ContractPrefillResponse getContractPrefill(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt phòng."));

        Room room = reservation.getRoom();
        if (room == null) throw new RuntimeException("Phiếu giữ phòng không có thông tin phòng.");

        // ADMIN: không giới hạn chi nhánh; MANAGER/RECEPTIONIST: chỉ xem chi nhánh của mình
        if (!hasRole("ADMIN")) {
            String myBranch = getCurrentEmployeeBranchCode();
            String reservationBranch = room.getBranch() != null ? room.getBranch().getBranchCode() : room.getBranchCode();
            if (reservationBranch == null || !reservationBranch.equalsIgnoreCase(myBranch)) {
                throw new RuntimeException("Bạn không có quyền truy cập phiếu giữ phòng của chi nhánh khác.");
            }
        }

        Tenant tenant = reservation.getTenant();
        if (tenant == null) throw new RuntimeException("Phiếu giữ phòng không có thông tin khách.");

        ContractPrefillResponse dto = new ContractPrefillResponse();
        dto.setReservationId(reservation.getId());

        String branchCode = room.getBranch() != null ? room.getBranch().getBranchCode() : room.getBranchCode();
        dto.setBranchCode(branchCode);
        dto.setRoomNumber(room.getRoomNumber());

        dto.setTenantId(tenant.getId());
        dto.setTenantFullName(tenant.getFullName());
        dto.setTenantPhoneNumber(tenant.getPhoneNumber());
        dto.setTenantEmail(tenant.getEmail());
        dto.setTenantAddress(tenant.getAddress());
        dto.setTenantCccd(tenant.getCccd());
        dto.setStudentId(tenant.getStudentId());
        dto.setUniversity(tenant.getUniversity());

        dto.setDeposit(room.getPrice());
        dto.setStartDate(java.time.LocalDate.now());
        return dto;
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CREATE_CONTRACT, targetType = "RESERVATION", description = "Đánh dấu phiếu giữ phòng đã lập hợp đồng")
    public ReservationResponse markContracted(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt phòng."));

        Room room = reservation.getRoom();
        if (room == null) throw new RuntimeException("Phiếu giữ phòng không có thông tin phòng.");

        if (!hasRole("ADMIN")) {
            String myBranch = getCurrentEmployeeBranchCode();
            String reservationBranch = room.getBranch() != null ? room.getBranch().getBranchCode() : room.getBranchCode();
            if (reservationBranch == null || !reservationBranch.equalsIgnoreCase(myBranch)) {
                throw new RuntimeException("Bạn không có quyền thao tác phiếu giữ phòng của chi nhánh khác.");
            }
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new RuntimeException("Chỉ có thể lập hợp đồng từ phiếu đã xác nhận (RESERVED).");
        }

        reservation.setStatus(ReservationStatus.COMPLETED);
        Reservation saved = reservationRepository.save(reservation);

        // Không giải phóng phòng; đảm bảo phòng đang OCCUPIED
        if (room != null) {
            room.setStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }

        return reservationMapper.toResponse(saved);
    }
}