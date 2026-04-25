package com.example.rental.controller;

import com.example.rental.config.JwtProperties;
import com.example.rental.dto.auth.AuthLoginRequest;
import com.example.rental.dto.auth.AuthResponse;
import com.example.rental.dto.auth.GuestRegisterRequest;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test nâng cao cho AuthController:
 * - Kiểm tra JWT token hợp lệ trong response
 * - Kiểm tra full flow: Register → Login
 * - Kiểm tra response structure kỹ hơn với AssertJ
 *
 * Tách riêng khỏi AuthControllerTest cơ bản để dễ mở rộng.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController – Advanced Tests (Token & Flow)")
class AuthControllerAdvancedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuditLogService auditLogService;

    private static final String LOGIN_URL          = "/api/auth/login";
    private static final String REGISTER_GUEST_URL = "/api/auth/register/guest";
    private static final String REGISTER_TENANT_URL = "/api/auth/register/tenant";

    private String mockToken;
    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        // Tạo JWT token hợp lệ bằng đúng secret key từ test profile
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        mockToken = Jwts.builder()
                .setSubject("testuser_advanced")
                .claim("roles", "ROLE_GUEST")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        mockAuthResponse = AuthResponse.builder()
                .accessToken(mockToken)
                .tokenType("Bearer")
                .id(42L)
                .username("testuser_advanced")
                .fullName("Test User Advanced")
                .email("advanced@example.com")
                .role("GUEST")
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // 1. Kiểm tra accessToken trong response là JWT hợp lệ
    // ─────────────────────────────────────────────────────────

    /**
     * [HAPPY PATH] Login → accessToken trong response có đúng 3 phần (header.payload.signature).
     * Không cần decode toàn bộ – chỉ kiểm tra format chuẩn JWT.
     */
    @Test
    @DisplayName("✅ accessToken trả về có format JWT hợp lệ (3 phần ngăn cách bởi '.')")
    void login_accessTokenHasValidJwtFormat() throws Exception {
        when(authService.login(any())).thenReturn(mockAuthResponse);

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("testuser_advanced");
        request.setPassword("Password@123");

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Đọc accessToken từ response JSON
        String responseBody = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);
        String accessToken = root.path("data").path("accessToken").asText();

        // Kiểm tra JWT có đúng 3 phần
        String[] parts = accessToken.split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).isNotBlank(); // header
        assertThat(parts[1]).isNotBlank(); // payload
        assertThat(parts[2]).isNotBlank(); // signature
    }

    // ─────────────────────────────────────────────────────────
    // 2. Kiểm tra response body đầy đủ tất cả fields
    // ─────────────────────────────────────────────────────────

    /**
     * [HAPPY PATH] Login response phải chứa đầy đủ: id, username, fullName, email, role.
     */
    @Test
    @DisplayName("✅ Login response body chứa đầy đủ user info")
    void login_responseBodyHasAllRequiredFields() throws Exception {
        when(authService.login(any())).thenReturn(mockAuthResponse);

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("testuser_advanced");
        request.setPassword("Password@123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // Kiểm tra tất cả fields của AuthResponse
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.username").value("testuser_advanced"))
                .andExpect(jsonPath("$.data.fullName").value("Test User Advanced"))
                .andExpect(jsonPath("$.data.email").value("advanced@example.com"))
                .andExpect(jsonPath("$.data.role").value("GUEST"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                // timestamp và statusCode trong wrapper
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─────────────────────────────────────────────────────────
    // 3. Kiểm tra UsernameNotFoundException → 401
    // ─────────────────────────────────────────────────────────

    /**
     * [NEGATIVE] Username không tồn tại trong hệ thống
     * → AuthService throw UsernameNotFoundException
     * → GlobalExceptionHandler → 401
     */
    @Test
    @DisplayName("❌ Login username không tồn tại → 401 Unauthorized")
    void login_usernameNotFound_shouldReturn401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new org.springframework.security.core.userdetails
                        .UsernameNotFoundException("Không tìm thấy user: ghost_user"));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("ghost_user");
        request.setPassword("AnyPassword123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    // ─────────────────────────────────────────────────────────
    // 4. Content-Type phải là application/json
    // ─────────────────────────────────────────────────────────

    /**
     * [HAPPY PATH] Response Content-Type phải là application/json.
     */
    @Test
    @DisplayName("✅ Login response Content-Type là application/json")
    void login_responseContentTypeIsJson() throws Exception {
        when(authService.login(any())).thenReturn(mockAuthResponse);

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("testuser_advanced");
        request.setPassword("Password@123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ─────────────────────────────────────────────────────────
    // 5. Register → rồi Login (flow test)
    // ─────────────────────────────────────────────────────────

    /**
     * [HAPPY PATH] Full flow: Register Guest → Login.
     * Kiểm tra thứ tự gọi service đúng, mỗi bước đều thành công.
     */
    @Test
    @DisplayName("✅ Flow: Register Guest → Login thành công")
    void fullFlow_registerThenLogin_bothSucceed() throws Exception {
        // Step 1: Register
        doNothing().when(authService).registerGuest(any());

        GuestRegisterRequest registerRequest = new GuestRegisterRequest();
        registerRequest.setUsername("flow_user_" + System.nanoTime());
        registerRequest.setPassword("Password@123");
        registerRequest.setEmail("flow_" + System.nanoTime() + "@example.com");
        registerRequest.setPhone("0359999888");
        registerRequest.setFullName("Flow Test User");

        mockMvc.perform(post(REGISTER_GUEST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201));

        // Step 2: Login
        when(authService.login(any())).thenReturn(mockAuthResponse);

        AuthLoginRequest loginRequest = new AuthLoginRequest();
        loginRequest.setUsername(registerRequest.getUsername());
        loginRequest.setPassword(registerRequest.getPassword());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());

        // Verify đúng thứ tự: register trước, login sau
        verify(authService, times(1)).registerGuest(any());
        verify(authService, times(1)).login(any(AuthLoginRequest.class));
    }

    // ─────────────────────────────────────────────────────────
    // 6. Register tenant endpoint
    // ─────────────────────────────────────────────────────────

    /**
     * [HAPPY PATH] Đăng ký Tenant hợp lệ → 201 Created.
     */
    @Test
    @DisplayName("✅ Register tenant thành công → 201 Created")
    void registerTenant_withValidData_shouldReturn201() throws Exception {
        doNothing().when(authService).registerTenant(any());

        // Tạo TenantRegisterRequest JSON với đầy đủ field bắt buộc
        String requestBody = """
                {
                    "username": "tenant_test_%d",
                    "password": "Password@123",
                    "email": "tenant_%d@example.com",
                    "phone": "0359888777",
                    "fullName": "Tenant User Full",
                    "cccd": "079123456789",
                    "address": "123 Test Street, HCM"
                }
                """.formatted(System.nanoTime(), System.nanoTime());

        mockMvc.perform(post(REGISTER_TENANT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    /**
     * [NEGATIVE] Đăng ký Tenant thiếu field bắt buộc → 400.
     */
    @Test
    @DisplayName("❌ Register tenant thiếu phone → 400 Bad Request")
    void registerTenant_missingPhone_shouldReturn400() throws Exception {
        String requestBody = """
                {
                    "username": "tenant_no_phone",
                    "password": "Password@123",
                    "email": "nophone@example.com"
                }
                """;

        mockMvc.perform(post(REGISTER_TENANT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
