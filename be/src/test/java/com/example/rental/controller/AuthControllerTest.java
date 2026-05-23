package com.example.rental.controller;

import com.example.rental.dto.auth.AuthLoginRequest;
import com.example.rental.dto.auth.AuthResponse;
import com.example.rental.dto.auth.EmployeeRegisterRequest;
import com.example.rental.dto.auth.GuestRegisterRequest;
import com.example.rental.dto.auth.PartnerRegisterRequest;
import com.example.rental.dto.auth.TenantRegisterRequest;
import com.example.rental.entity.EmployeePosition;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite cho AuthController.
 *
 * Chiến lược:
 *  - @SpringBootTest + @AutoConfigureMockMvc để load full context.
 *  - @ActiveProfiles("test") để dùng H2 in-memory (không cần MySQL).
 *  - @MockBean AuthService, AuditLogService → không chạm DB thật.
 *  - Mỗi nhóm test được tổ chức trong @Nested class theo chức năng.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController – Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ----- Mock dependencies -----
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuditLogService auditLogService;

    // ----- Constant test data (không hardcode phụ thuộc DB) -----
    private static final String LOGIN_URL          = "/api/auth/login";
    private static final String REGISTER_GUEST_URL = "/api/auth/register/guest";

    private GuestRegisterRequest validGuestRequest;
    private TenantRegisterRequest validTenantRequest;
    private PartnerRegisterRequest validPartnerRequest;
    private EmployeeRegisterRequest validEmployeeRequest;
    private AuthLoginRequest validLoginRequest;
    private AuthResponse mockAuthResponse;

    /**
     * Khởi tạo dữ liệu test dùng chung cho mọi test.
     * UUID / random suffix đảm bảo không bị conflict khi chạy song song.
     */
    @BeforeEach
    void setUp() {
        String uniqueSuffix = String.valueOf(System.nanoTime());

        // --- Valid guest register request ---
        validGuestRequest = new GuestRegisterRequest();
        validGuestRequest.setUsername("testuser_" + uniqueSuffix);
        validGuestRequest.setPassword("Password@123");
        validGuestRequest.setEmail("test_" + uniqueSuffix + "@example.com");
        validGuestRequest.setPhone("0359123456");
        validGuestRequest.setFullName("Nguyen Van Test");
        validGuestRequest.setDob("2000-01-15");

        // --- Valid tenant register request ---
        validTenantRequest = new TenantRegisterRequest();
        validTenantRequest.setUsername("tenant_" + uniqueSuffix);
        validTenantRequest.setPassword("Password@123");
        validTenantRequest.setEmail("tenant_" + uniqueSuffix + "@example.com");
        validTenantRequest.setPhone("0359123456");
        validTenantRequest.setFullName("Nguyen Van Tenant");
        validTenantRequest.setCccd("079123456789");
        validTenantRequest.setStudentId("SV" + uniqueSuffix);
        validTenantRequest.setUniversity("Dai hoc Bach Khoa");
        validTenantRequest.setAddress("123 Le Loi, Quan 1");

        // --- Valid partner register request ---
        validPartnerRequest = new PartnerRegisterRequest();
        validPartnerRequest.setUsername("partner_" + uniqueSuffix);
        validPartnerRequest.setPassword("Password@123");
        validPartnerRequest.setEmail("partner_" + uniqueSuffix + "@example.com");
        validPartnerRequest.setPhone("0359123456");
        validPartnerRequest.setFullName("Nguyen Van Partner");
        validPartnerRequest.setCompanyName("Nha tro X");
        validPartnerRequest.setAddress("TP HCM");
        validPartnerRequest.setTaxCode("TAX" + uniqueSuffix);

        // --- Valid employee register request ---
        validEmployeeRequest = new EmployeeRegisterRequest();
        validEmployeeRequest.setUsername("employee_" + uniqueSuffix);
        validEmployeeRequest.setPassword("Password@123");
        validEmployeeRequest.setEmail("employee_" + uniqueSuffix + "@example.com");
        validEmployeeRequest.setPhone("0359123456");
        validEmployeeRequest.setFullName("Nguyen Van Employee");
        validEmployeeRequest.setBranchCode("CN01");
        validEmployeeRequest.setPosition(EmployeePosition.RECEPTIONIST);
        validEmployeeRequest.setSalary(new BigDecimal("10000000"));
        validEmployeeRequest.setHireDate(LocalDate.now().minusDays(1));

        // --- Valid login request ---
        validLoginRequest = new AuthLoginRequest();
        validLoginRequest.setUsername("testuser_" + uniqueSuffix);
        validLoginRequest.setPassword("Password@123");

        // --- Mock AuthResponse trả về khi login thành công ---
        mockAuthResponse = AuthResponse.builder()
                .accessToken("mock.jwt.token." + uniqueSuffix)
                .tokenType("Bearer")
                .id(1L)
                .username(validLoginRequest.getUsername())
                .fullName("Nguyen Van Test")
                .email("test_" + uniqueSuffix + "@example.com")
                .role("GUEST")
                .build();
    }

    // =========================================================
    // 1. HAPPY PATH – Login
    // =========================================================
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        /**
         * [HAPPY PATH] Login đúng username + password
         * → HTTP 200, body chứa accessToken và tokenType.
         */
        @Test
        @DisplayName("✅ Login thành công → 200 + accessToken trong response")
        void loginSuccess_shouldReturn200WithAccessToken() throws Exception {
            // Arrange: AuthService trả về mock response
            when(authService.login(any(AuthLoginRequest.class))).thenReturn(mockAuthResponse);

            String requestBody = objectMapper.writeValueAsString(validLoginRequest);

            // Act + Assert
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    // Kiểm tra wrapper ApiResponseDto
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    // Kiểm tra data chứa accessToken
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.role").value("GUEST"));

            // Verify: AuthService.login() được gọi đúng 1 lần
            verify(authService, times(1)).login(any(AuthLoginRequest.class));
        }

        /**
         * [NEGATIVE] Login sai password
         * → AuthService ném BadCredentialsException
         * → GlobalExceptionHandler bắt → HTTP 401
         */
        @Test
        @DisplayName("❌ Login sai password → 401 Unauthorized")
        void loginWrongPassword_shouldReturn401() throws Exception {
            // Arrange: Service throw exception khi sai password
            when(authService.login(any(AuthLoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Mật khẩu không đúng"));

            AuthLoginRequest wrongRequest = new AuthLoginRequest();
            wrongRequest.setUsername(validLoginRequest.getUsername());
            wrongRequest.setPassword("WRONG_PASSWORD_999");

            // Act + Assert
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.statusCode").value(401));
        }

        /**
         * [NEGATIVE] Login thiếu field `username`
         * → Bean Validation bắt @NotBlank → HTTP 400
         */
        @Test
        @DisplayName("❌ Login thiếu username → 400 Bad Request")
        void loginMissingUsername_shouldReturn400() throws Exception {
            // Arrange: Body thiếu username
            String requestBody = """
                    {
                        "password": "Password@123"
                    }
                    """;

            // Act + Assert
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));

            // Service không được gọi khi validation fail
            verifyNoInteractions(authService);
        }

        /**
         * [NEGATIVE] Login thiếu field `password`
         * → Bean Validation → HTTP 400
         */
        @Test
        @DisplayName("❌ Login thiếu password → 400 Bad Request")
        void loginMissingPassword_shouldReturn400() throws Exception {
            String requestBody = """
                    {
                        "username": "someuser"
                    }
                    """;

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));

            verifyNoInteractions(authService);
        }

        /**
         * [NEGATIVE] Body rỗng hoàn toàn → HTTP 400
         */
        @Test
        @DisplayName("❌ Login body rỗng → 400 Bad Request")
        void loginEmptyBody_shouldReturn400() throws Exception {
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }
    }

    // =========================================================
    // 2. HAPPY PATH – Register (Guest)
    // =========================================================
    @Nested
    @DisplayName("POST /api/auth/register/guest")
    class RegisterGuestTests {

        /**
         * [HAPPY PATH] Đăng ký Guest hợp lệ
         * → HTTP 201, message "Đăng ký tài khoản khách thành công"
         */
        @Test
        @DisplayName("✅ Register guest thành công → 201 Created")
        void registerGuest_withValidData_shouldReturn201() throws Exception {
            // Arrange: Service không throw exception → đăng ký thành công
            doNothing().when(authService).registerGuest(any());

            // Act + Assert
            mockMvc.perform(post(REGISTER_GUEST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGuestRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Đăng ký tài khoản khách thành công"));

            verify(authService, times(1)).registerGuest(any());
        }

        /**
         * [NEGATIVE] Đăng ký với username đã tồn tại
         * → Service throw IllegalArgumentException
         * → GlobalExceptionHandler → HTTP 400
         *
         * Lưu ý: Dự án dùng IllegalArgumentException cho "đã tồn tại",
         * nếu muốn trả 409 cần thêm custom exception. Test này phản ánh
         * behavior hiện tại (400). Có thể điều chỉnh khi refactor.
         */
        @Test
        @DisplayName("❌ Đăng ký username đã tồn tại → 400 (duplicate)")
        void registerGuest_duplicateUsername_shouldReturn400() throws Exception {
            // Arrange: Simulate username đã tồn tại
            doThrow(new IllegalArgumentException("Username đã được sử dụng"))
                    .when(authService).registerGuest(any());

            mockMvc.perform(post(REGISTER_GUEST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGuestRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }

        /**
         * [NEGATIVE] Đăng ký thiếu field `username`
         * → Bean Validation → HTTP 400
         */
        @Test
        @DisplayName("❌ Register thiếu username → 400 Bad Request")
        void registerGuest_missingUsername_shouldReturn400() throws Exception {
            // Xoá username để kích hoạt @NotBlank
            validGuestRequest.setUsername(null);

            mockMvc.perform(post(REGISTER_GUEST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGuestRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.error").exists());

            verifyNoInteractions(authService);
        }

        /**
         * [NEGATIVE] Đăng ký thiếu field `email`
         * → Bean Validation @NotBlank + @Email → HTTP 400
         */
        @Test
        @DisplayName("❌ Register thiếu email → 400 Bad Request")
        void registerGuest_missingEmail_shouldReturn400() throws Exception {
            validGuestRequest.setEmail(null);

            mockMvc.perform(post(REGISTER_GUEST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGuestRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        /**
         * [NEGATIVE] Đăng ký với email không đúng format
         * → Bean Validation @Email → HTTP 400
         */
        @Test
        @DisplayName("❌ Register email sai format → 400 Bad Request")
        void registerGuest_invalidEmailFormat_shouldReturn400() throws Exception {
            validGuestRequest.setEmail("not-an-email");

            mockMvc.perform(post(REGISTER_GUEST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGuestRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        /**
         * [NEGATIVE] Đăng ký với số điện thoại sai format Việt Nam
         * → Bean Validation @Pattern → HTTP 400
         */
        @Test
        @DisplayName("❌ Register phone sai format → 400 Bad Request")
        void registerGuest_invalidPhoneFormat_shouldReturn400() throws Exception {
            validGuestRequest.setPhone("123"); // Số không hợp lệ

            mockMvc.perform(post(REGISTER_GUEST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGuestRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        /**
         * [NEGATIVE] Đăng ký thiếu fullName (field bắt buộc của GuestRegisterRequest)
         * → HTTP 400
         */
        @Test
        @DisplayName("❌ Register thiếu fullName → 400 Bad Request")
        void registerGuest_missingFullName_shouldReturn400() throws Exception {
            validGuestRequest.setFullName(null);

            mockMvc.perform(post(REGISTER_GUEST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGuestRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }
    }

    // =========================================================
    // 3. Register – Tenant
    // =========================================================
    @Nested
    @DisplayName("POST /api/auth/register/tenant")
    class RegisterTenantTests {

        @Test
        @DisplayName("✅ Register tenant hợp lệ → 201 Created")
        void registerTenant_withValidData_shouldReturn201() throws Exception {
            doNothing().when(authService).registerTenant(any());

            mockMvc.perform(post("/api/auth/register/tenant")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTenantRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Tenant registered successfully"));

            verify(authService, times(1)).registerTenant(any());
        }

        @Test
        @DisplayName("❌ Register tenant thiếu fullName → 400 Bad Request")
        void registerTenant_missingFullName_shouldReturn400() throws Exception {
            validTenantRequest.setFullName(null);

            mockMvc.perform(post("/api/auth/register/tenant")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTenantRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("❌ Register tenant thiếu CCCD → 400 Bad Request")
        void registerTenant_missingCccd_shouldReturn400() throws Exception {
            validTenantRequest.setCccd(null);

            mockMvc.perform(post("/api/auth/register/tenant")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTenantRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("❌ Register tenant thiếu address → 400 Bad Request")
        void registerTenant_missingAddress_shouldReturn400() throws Exception {
            validTenantRequest.setAddress(null);

            mockMvc.perform(post("/api/auth/register/tenant")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTenantRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }
    }

    // =========================================================
    // 4. Register – Partner
    // =========================================================
    @Nested
    @DisplayName("POST /api/auth/register/partner")
    class RegisterPartnerTests {

        @Test
        @DisplayName("✅ Register partner hợp lệ → 201 Created")
        void registerPartner_withValidData_shouldReturn201() throws Exception {
            doNothing().when(authService).registerPartner(any());

            mockMvc.perform(post("/api/auth/register/partner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPartnerRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Đăng ký tài khoản chủ trọ thành công"));

            verify(authService, times(1)).registerPartner(any());
        }

        @Test
        @DisplayName("❌ Register partner thiếu fullName → 400 Bad Request")
        void registerPartner_missingFullName_shouldReturn400() throws Exception {
            validPartnerRequest.setFullName(null);

            mockMvc.perform(post("/api/auth/register/partner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPartnerRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }
    }

    // =========================================================
    // 5. Register – Employee
    // =========================================================
    @Nested
    @DisplayName("POST /api/auth/register/employee")
    class RegisterEmployeeTests {

        @Test
        @DisplayName("✅ Register employee hợp lệ → 201 Created")
        void registerEmployee_withValidData_shouldReturn201() throws Exception {
            doNothing().when(authService).registerEmployee(any());

            mockMvc.perform(post("/api/auth/register/employee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validEmployeeRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Employee registered successfully"));

            verify(authService, times(1)).registerEmployee(any());
        }

        @Test
        @DisplayName("❌ Register employee thiếu branchCode → 400 Bad Request")
        void registerEmployee_missingBranchCode_shouldReturn400() throws Exception {
            validEmployeeRequest.setBranchCode(null);

            mockMvc.perform(post("/api/auth/register/employee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validEmployeeRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("❌ Register employee thiếu position → 400 Bad Request")
        void registerEmployee_missingPosition_shouldReturn400() throws Exception {
            validEmployeeRequest.setPosition(null);

            mockMvc.perform(post("/api/auth/register/employee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validEmployeeRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }
    }

    // =========================================================
    // 6. POST /api/auth/logout
    // =========================================================
    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @WithMockUser(username = "tester", roles = "ADMIN")
        @DisplayName("✅ Logout có auth → 200 + ghi audit log")
        void logout_withAuth_shouldReturn200() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Logout logged"))
                    .andExpect(jsonPath("$.data").value("OK"));

            var usernameCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
            var roleCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
            verify(auditLogService, times(1)).logAction(
                    usernameCaptor.capture(),
                    roleCaptor.capture(),
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            );

            Assertions.assertThat(usernameCaptor.getValue()).isEqualTo("tester");
            Assertions.assertThat(roleCaptor.getValue()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("✅ Logout không auth → 200 + audit ANONYMOUS")
        void logout_withoutAuth_shouldReturn200() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200));

            var usernameCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
            var roleCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
            verify(auditLogService, times(1)).logAction(
                    usernameCaptor.capture(),
                    roleCaptor.capture(),
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            );

            Assertions.assertThat(usernameCaptor.getValue()).isEqualTo("ANONYMOUS");
            Assertions.assertThat(roleCaptor.getValue()).isEqualTo("ANONYMOUS");
        }
    }
}
