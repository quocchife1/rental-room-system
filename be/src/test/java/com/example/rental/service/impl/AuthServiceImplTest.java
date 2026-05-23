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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
//import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    // ==========================================
    // TESTS FOR LOGIN
    // ==========================================

    private Authentication setupMockAuthentication(String username, String role) {
        Authentication authMock = mock(Authentication.class);
        CustomUserDetails userDetailsMock = mock(CustomUserDetails.class);
        
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        doReturn(Collections.singletonList(authority)).when(userDetailsMock).getAuthorities();
        when(userDetailsMock.getUsername()).thenReturn(username);
        
        when(authMock.getPrincipal()).thenReturn(userDetailsMock);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(jwtProvider.generateAccessToken(authMock)).thenReturn("mock-jwt-token");
        
        return authMock;
    }

    @Test
    void login_Employee_Success() {
        setupMockAuthentication("emp1", "ROLE_ADMIN");

        Employees emp = new Employees();
        emp.setId(1L);
        emp.setFullName("Employee Name");
        emp.setEmail("emp@test.com");
        emp.setPhoneNumber("123456789");
        when(employeeRepository.findByUsername("emp1")).thenReturn(Optional.of(emp));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("emp1");
        request.setPassword("password");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getFullName()).isEqualTo("Employee Name");
        assertThat(response.getRole()).isEqualTo("ADMIN");

        verify(auditLogService, times(1)).logAction(eq("emp1"), eq("ADMIN"), eq(AuditAction.LOGIN_SUCCESS), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void login_Tenant_Success() {
        setupMockAuthentication("tenant1", "ROLE_TENANT");

        Tenant tenant = new Tenant();
        tenant.setId(2L);
        tenant.setFullName("Tenant Name");
        when(tenantRepository.findByUsername("tenant1")).thenReturn(Optional.of(tenant));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("tenant1");

        AuthResponse response = authService.login(request);

        assertThat(response.getFullName()).isEqualTo("Tenant Name");
        assertThat(response.getRole()).isEqualTo("TENANT");
    }

    @Test
    void login_Partner_Success() {
        setupMockAuthentication("partner1", "ROLE_PARTNER");

        Partners partner = new Partners();
        partner.setId(3L);
        partner.setContactPerson("Partner Contact");
        when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("partner1");

        AuthResponse response = authService.login(request);

        assertThat(response.getFullName()).isEqualTo("Partner Contact");
        assertThat(response.getRole()).isEqualTo("PARTNER");
    }

    @Test
    void login_Guest_Success_NoFullNameFallback() {
        setupMockAuthentication("guest1", "ROLE_GUEST");

        Guest guest = new Guest();
        guest.setId(4L);
        guest.setFullName(""); // Cố tình để rỗng để test logic fallback username
        when(guestRepository.findByUsername("guest1")).thenReturn(Optional.of(guest));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("guest1");

        AuthResponse response = authService.login(request);

        // Fallback to username
        assertThat(response.getFullName()).isEqualTo("guest1");
        assertThat(response.getRole()).isEqualTo("GUEST");
    }

    @Test
    void login_Failure_ThrowsException() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("wronguser");
        request.setPassword("wrongpass");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        // Verify audit log cho trường hợp thất bại
        verify(auditLogService, times(1)).logAction(eq("wronguser"), eq("ANONYMOUS"), eq(AuditAction.LOGIN_FAILED), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ==========================================
    // TESTS FOR GUEST REGISTRATION
    // ==========================================

    @Test
    void registerGuest_Success_WithValidDob() {
        GuestRegisterRequest request = new GuestRegisterRequest();
        request.setUsername("newguest");
        request.setPassword("pass");
        request.setEmail("guest@test.com");
        request.setDob("2000-01-01");

        when(guestRepository.findByUsername("newguest")).thenReturn(Optional.empty());
        when(guestRepository.existsByEmail("guest@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        authService.registerGuest(request);

        verify(guestRepository, times(1)).save(any(Guest.class));
    }

    @Test
    void registerGuest_Success_WithInvalidDob_Fallback() {
        GuestRegisterRequest request = new GuestRegisterRequest();
        request.setUsername("newguest");
        request.setDob("invalid-date-format"); // Cố tình truyền sai định dạng để phủ nhánh catch

        when(guestRepository.findByUsername("newguest")).thenReturn(Optional.empty());
        
        authService.registerGuest(request);

        verify(guestRepository, times(1)).save(any(Guest.class)); // Vẫn save bình thường
    }

    @Test
    void registerGuest_Fail_UsernameExists() {
        GuestRegisterRequest request = new GuestRegisterRequest();
        request.setUsername("existguest");

        when(guestRepository.findByUsername("existguest")).thenReturn(Optional.of(new Guest()));

        assertThatThrownBy(() -> authService.registerGuest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tên đăng nhập đã tồn tại");
    }

    @Test
    void registerGuest_Fail_EmailExists() {
        GuestRegisterRequest request = new GuestRegisterRequest();
        request.setUsername("newguest");
        request.setEmail("exist@test.com");

        when(guestRepository.findByUsername("newguest")).thenReturn(Optional.empty());
        when(guestRepository.existsByEmail("exist@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerGuest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email đã tồn tại");
    }

    // ==========================================
    // TESTS FOR TENANT REGISTRATION
    // ==========================================

    @Test
    void registerTenant_Success() {
        TenantRegisterRequest request = new TenantRegisterRequest();
        request.setUsername("newtenant");
        request.setPassword("pass");

        when(tenantRepository.findByUsername("newtenant")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        authService.registerTenant(request);

        verify(tenantRepository, times(1)).save(any(Tenant.class));
    }

    @Test
    void registerTenant_Fail_UsernameExists() {
        TenantRegisterRequest request = new TenantRegisterRequest();
        request.setUsername("existtenant");

        when(tenantRepository.findByUsername("existtenant")).thenReturn(Optional.of(new Tenant()));

        assertThatThrownBy(() -> authService.registerTenant(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void registerTenant_Fail_InvalidRequestType() {
        // Cố tình truyền sai loại class vào hàm
        AuthRegisterRequest invalidRequest = new GuestRegisterRequest();
        invalidRequest.setUsername("test");

        when(tenantRepository.findByUsername("test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.registerTenant(invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid request data for Tenant registration");
    }

    // ==========================================
    // TESTS FOR PARTNER REGISTRATION
    // ==========================================

    @Test
    void registerPartner_Success_AllFields() {
        PartnerRegisterRequest request = new PartnerRegisterRequest();
        request.setUsername("newpartner");
        request.setCompanyName("Company A");
        request.setAddress("123 Street");
        request.setPassword("pass");

        when(partnerRepository.findByUsername("newpartner")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        authService.registerPartner(request);

        verify(partnerRepository, times(1)).save(any(Partners.class));
    }

    @Test
    void registerPartner_Success_MissingCompanyAndAddress() {
        PartnerRegisterRequest request = new PartnerRegisterRequest();
        request.setUsername("newpartner");
        request.setFullName("Partner Name");
        // Không truyền CompanyName và Address để test logic fallback

        when(partnerRepository.findByUsername("newpartner")).thenReturn(Optional.empty());

        authService.registerPartner(request);

        verify(partnerRepository, times(1)).save(argThat(partner -> 
            partner.getCompanyName().equals("Partner Name") &&
            partner.getAddress().equals("Đang cập nhật")
        ));
    }

    @Test
    void registerPartner_Fail_UsernameExists() {
        PartnerRegisterRequest request = new PartnerRegisterRequest();
        request.setUsername("existpartner");

        when(partnerRepository.findByUsername("existpartner")).thenReturn(Optional.of(new Partners()));

        assertThatThrownBy(() -> authService.registerPartner(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tên đăng nhập đã tồn tại");
    }

    // ==========================================
    // TESTS FOR EMPLOYEE REGISTRATION
    // ==========================================

    @Test
    void registerEmployee_Success() {
        EmployeeRegisterRequest request = new EmployeeRegisterRequest();
        request.setUsername("newemp");
        request.setPassword("pass");

        when(employeeRepository.findByUsername("newemp")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        authService.registerEmployee(request);

        verify(employeeRepository, times(1)).save(any(Employees.class));
    }

    @Test
    void registerEmployee_Fail_UsernameExists() {
        EmployeeRegisterRequest request = new EmployeeRegisterRequest();
        request.setUsername("existemp");

        when(employeeRepository.findByUsername("existemp")).thenReturn(Optional.of(new Employees()));

        assertThatThrownBy(() -> authService.registerEmployee(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
    }
}
