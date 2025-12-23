package com.example.rental.service.impl;

import com.example.rental.dto.auth.*;
import com.example.rental.entity.*;
import com.example.rental.exception.BadRequestException;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.repository.GuestRepository;
import com.example.rental.repository.PartnerRepository;
import com.example.rental.repository.TenantRepository;
import com.example.rental.security.CustomUserDetails;
import com.example.rental.security.JwtProvider;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    private final AuditLogService auditLogService;

    private final EmployeeRepository employeeRepository;
    private final TenantRepository tenantRepository;
    private final PartnerRepository partnerRepository;
    private final GuestRepository guestRepository;

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        Authentication authentication;
        try {
            // 1. Xác thực (Authentication)
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception ex) {
            // Record failed login attempts
            auditLogService.logAction(
                request.getUsername() != null ? request.getUsername() : "UNKNOWN",
                "ANONYMOUS",
                AuditAction.LOGIN_FAILED,
                "AUTH",
                null,
                "Đăng nhập thất bại",
                null,
                null,
                "unknown",
                null,
                "unknown",
                "FAILURE",
                ex.getMessage()
            );
            throw ex;
        }
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Tạo Token
        String jwt = jwtProvider.generateAccessToken(authentication);
        
        // 3. Lấy thông tin user từ Authentication
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // 4. Lấy thông tin chi tiết
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("GUEST");
                
        String username = userDetails.getUsername();

        Long id = null;
        String fullName = username; 
        String email = "";
        String phone = "";
        String address = "";

        // Employees: authority is ROLE_<EmployeePosition> (e.g. ROLE_ACCOUNTANT, ROLE_MAINTENANCE, ROLE_ADMIN...)
        Optional<Employees> emp = employeeRepository.findByUsername(username);
        if (emp.isPresent()) {
            id = emp.get().getId();
            fullName = emp.get().getFullName();
            email = emp.get().getEmail();
            phone = emp.get().getPhoneNumber();
        } else if (role.contains("TENANT")) {
            Optional<Tenant> tenant = tenantRepository.findByUsername(username);
            if (tenant.isPresent()) {
                id = tenant.get().getId();
                fullName = tenant.get().getFullName();
                email = tenant.get().getEmail();
                phone = tenant.get().getPhoneNumber();
                address = tenant.get().getAddress();
            }
        } else if (role.contains("PARTNER")) {
            Optional<Partners> partner = partnerRepository.findByUsername(username);
            if (partner.isPresent()) {
                id = partner.get().getId();
                fullName = partner.get().getContactPerson() != null ? partner.get().getContactPerson() : partner.get().getCompanyName();
                email = partner.get().getEmail();
                phone = partner.get().getPhoneNumber();
                address = partner.get().getAddress();
            }
        } else if (role.contains("GUEST")) {
            Optional<Guest> guest = guestRepository.findByUsername(username);
            if (guest.isPresent()) {
                id = guest.get().getId();
                fullName = guest.get().getFullName();
                email = guest.get().getEmail();
                phone = guest.get().getPhoneNumber();
            }
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = username;
        }

        String cleanRole = role.replace("ROLE_", "");

        // Record successful login
        auditLogService.logAction(
            username,
            cleanRole,
            AuditAction.LOGIN_SUCCESS,
            "AUTH",
            null,
            "Đăng nhập thành công",
            null,
            "{\"role\":\"" + cleanRole + "\"}",
            "unknown",
            null,
            "unknown",
            "SUCCESS",
            null
        );

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .id(id)
                .username(username)
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .address(address)
                .role(cleanRole)
                .build();
    }

    @Override
    public void registerGuest(AuthRegisterRequest request) {
        // Ép kiểu để lấy dữ liệu riêng của Guest
        GuestRegisterRequest guestRequest = (GuestRegisterRequest) request;

        if (guestRepository.findByUsername(guestRequest.getUsername()).isPresent()) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }
        if (guestRepository.existsByEmail(guestRequest.getEmail())) {
             throw new BadRequestException("Email đã tồn tại");
        }

        Guest guest = new Guest();
        guest.setUsername(guestRequest.getUsername());
        guest.setPassword(passwordEncoder.encode(guestRequest.getPassword()));
        guest.setEmail(guestRequest.getEmail());
        guest.setFullName(guestRequest.getFullName());
        guest.setPhoneNumber(guestRequest.getPhone());
        guest.setStatus(UserStatus.ACTIVE);

        // Xử lý ngày sinh (dob) từ String sang LocalDate
        if (guestRequest.getDob() != null && !guestRequest.getDob().isEmpty()) {
            try {
                // Giả sử client gửi yyyy-MM-dd. Nếu client gửi dd/MM/yyyy cần format lại
                guest.setDob(java.time.LocalDate.parse(guestRequest.getDob())); 
            } catch (Exception e) {
                // Log error hoặc bỏ qua nếu sai định dạng
            }
        }
        
        guestRepository.save(guest);
    }

    @Override
    // SỬA LỖI: Tham số phải là AuthRegisterRequest để khớp với Interface AuthService
    public void registerTenant(AuthRegisterRequest request) {
        if (tenantRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        // Ép kiểu về TenantRegisterRequest để lấy các trường riêng (cccd, studentId...)
        // Vì Controller truyền vào TenantRegisterRequest nên việc cast này an toàn
        TenantRegisterRequest tenantRequest;
        if (request instanceof TenantRegisterRequest) {
            tenantRequest = (TenantRegisterRequest) request;
        } else {
            // Fallback nếu request không đúng kiểu (hiếm khi xảy ra nếu controller đúng)
            throw new BadRequestException("Invalid request data for Tenant registration");
        }

        Tenant tenant = new Tenant();
        tenant.setUsername(tenantRequest.getUsername());
        tenant.setPassword(passwordEncoder.encode(tenantRequest.getPassword()));
        tenant.setEmail(tenantRequest.getEmail());
        tenant.setFullName(tenantRequest.getFullName());
        
        // SỬA LỖI: DTO dùng 'phone', Entity dùng 'phoneNumber'
        tenant.setPhoneNumber(tenantRequest.getPhone()); 
        
        // Các trường riêng của Tenant
        tenant.setAddress(tenantRequest.getAddress());
        tenant.setCccd(tenantRequest.getCccd());
        tenant.setStudentId(tenantRequest.getStudentId());
        tenant.setUniversity(tenantRequest.getUniversity());
        
        tenant.setStatus(UserStatus.ACTIVE);
        tenantRepository.save(tenant);
    }

    @Override
    public void registerPartner(PartnerRegisterRequest request) {
        if (partnerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }

        Partners partner = new Partners();
        partner.setUsername(request.getUsername());
        partner.setPassword(passwordEncoder.encode(request.getPassword()));
        partner.setEmail(request.getEmail());
        partner.setPhoneNumber(request.getPhone());
        partner.setContactPerson(request.getFullName()); // Map Họ tên vào người liên hệ
        
        // --- XỬ LÝ FIX LỖI 400 KHI THIẾU DỮ LIỆU ---
        // Nếu form không có tên công ty, lấy luôn họ tên làm tên công ty
        partner.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : request.getFullName());
        
        // Nếu form không có địa chỉ, đặt tạm để không lỗi database
        partner.setAddress(request.getAddress() != null ? request.getAddress() : "Đang cập nhật"); 
        
        partner.setStatus(UserStatus.ACTIVE);
        partnerRepository.save(partner);
    }

    @Override
    public void registerEmployee(EmployeeRegisterRequest request) {
        if (employeeRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        Employees employee = new Employees();
        employee.setUsername(request.getUsername());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setEmail(request.getEmail());
        // SỬA LỖI: DTO dùng 'phone'
        employee.setPhoneNumber(request.getPhone());
        employee.setFullName(request.getFullName());
        
        employee.setStatus(UserStatus.ACTIVE);
        employeeRepository.save(employee);
    }
}