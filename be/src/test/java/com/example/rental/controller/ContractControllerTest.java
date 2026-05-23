package com.example.rental.controller;

import com.example.rental.dto.checkout.CheckoutRequestDto;
import com.example.rental.dto.checkout.CheckoutRequestResponse;
import com.example.rental.dto.contract.ContractCreateRequest;
import com.example.rental.dto.contract.ContractResponse;
import com.example.rental.dto.contract.ContractUpdateRequest;
import com.example.rental.dto.contract.DepositMomoInitiateRequest;
import com.example.rental.dto.contract.DepositPaymentRequest;
import com.example.rental.dto.momo.CreateMomoResponse;
import com.example.rental.entity.Contract;
import com.example.rental.entity.ContractStatus;
import com.example.rental.entity.PaymentMethod;
import com.example.rental.entity.Tenant;
import com.example.rental.mapper.ContractMapper;
import com.example.rental.repository.ContractRepository;
import com.example.rental.service.CheckoutService;
import com.example.rental.service.ContractService;
import com.example.rental.service.MomoService;
import com.example.rental.service.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ContractController – Integration Tests")
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContractService contractService;

    @MockitoBean
    private ContractMapper contractMapper;

    @MockitoBean
    private MomoService momoService;

    @MockitoBean
    private ContractRepository contractRepository;

    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private CheckoutService checkoutService;

    private Contract sampleContract;
    private ContractResponse sampleResponse;
    private ContractCreateRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        sampleContract = Contract.builder()
                .id(1L)
                .branchCode("CN01")
                .roomNumber("101")
                .status(ContractStatus.PENDING)
                .deposit(new BigDecimal("1000000"))
                .startDate(LocalDate.now())
                .build();

        sampleResponse = new ContractResponse();
        sampleResponse.setId(1L);
        sampleResponse.setBranchCode("CN01");
        sampleResponse.setRoomNumber("101");
        sampleResponse.setStatus("PENDING");

        validCreateRequest = new ContractCreateRequest();
        validCreateRequest.setBranchCode("CN01");
        validCreateRequest.setRoomNumber("101");
        validCreateRequest.setTenantFullName("Nguyen Van Test");
        validCreateRequest.setTenantPhoneNumber("0359123456");
        validCreateRequest.setTenantEmail("test@example.com");
        validCreateRequest.setDeposit(new BigDecimal("1000000"));
        validCreateRequest.setStartDate(LocalDate.now());
        validCreateRequest.setEndDate(LocalDate.now().plusMonths(6));
    }

    // =========================================================
    // 1. POST /api/contracts – Create contract
    // =========================================================
    @Nested
    @DisplayName("POST /api/contracts")
    class CreateContractTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Create contract → 201")
        void createContract_shouldReturn201() throws Exception {
            when(contractService.createContract(any(ContractCreateRequest.class))).thenReturn(sampleContract);
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/contracts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("❌ Create contract without auth → 403")
        void createContract_withoutAuth_shouldReturn403() throws Exception {
            mockMvc.perform(post("/api/contracts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 2. GET /api/contracts/my-branch
    // =========================================================
    @Nested
    @DisplayName("GET /api/contracts/my-branch")
    class GetMyBranchContractsTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Get my-branch contracts → 200 + page")
        void getMyBranchContracts_shouldReturn200() throws Exception {
            Page<Contract> page = new PageImpl<>(List.of(sampleContract));
            when(contractService.getMyBranchContracts(eq("PENDING"), eq("CN01"), any())).thenReturn(page);
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/contracts/my-branch")
                            .param("status", "PENDING")
                            .param("q", "CN01")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].id").value(1));
        }
    }

    // =========================================================
    // 3. GET /api/contracts/{id}
    // =========================================================
    @Nested
    @DisplayName("GET /api/contracts/{id}")
    class GetContractForStaffTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Get contract detail → 200")
        void getContractForStaff_shouldReturn200() throws Exception {
            when(contractService.getContractForStaff(1L)).thenReturn(sampleContract);
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/contracts/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    // =========================================================
    // 4. PUT /api/contracts/{id}
    // =========================================================
    @Nested
    @DisplayName("PUT /api/contracts/{id}")
    class UpdateContractTests {

        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("✅ Update contract → 200")
        void updateContract_shouldReturn200() throws Exception {
            ContractUpdateRequest request = new ContractUpdateRequest();
            request.setTenantFullName("Updated Name");

            when(contractService.updateContractForStaff(eq(1L), any(ContractUpdateRequest.class))).thenReturn(sampleContract);
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/contracts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Cập nhật hợp đồng thành công"));
        }
    }

    // =========================================================
    // 5. DELETE /api/contracts/{id}
    // =========================================================
    @Nested
    @DisplayName("DELETE /api/contracts/{id}")
    class DeleteContractTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("✅ Delete pending contract → 200")
        void deletePendingContract_shouldReturn200() throws Exception {
            doNothing().when(contractService).deletePendingContractForStaff(1L);

            mockMvc.perform(delete("/api/contracts/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Đã xóa hợp đồng tạm."));
        }
    }

    // =========================================================
    // 6. POST /api/contracts/{id}/upload-signed
    // =========================================================
    @Nested
    @DisplayName("POST /api/contracts/{id}/upload-signed")
    class UploadSignedContractTests {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("✅ Upload signed contract → 200")
        void uploadSignedContract_shouldReturn200() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "contract.pdf",
                    MediaType.APPLICATION_PDF_VALUE,
                    "dummy".getBytes()
            );

            when(contractService.uploadSignedContract(eq(1L), any())).thenReturn(sampleContract);
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/contracts/1/upload-signed")
                            .file(file))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Upload hợp đồng đã ký thành công"));
        }
    }

    // =========================================================
    // 7. POST /api/contracts/{id}/deposit/confirm
    // =========================================================
    @Nested
    @DisplayName("POST /api/contracts/{id}/deposit/confirm")
    class ConfirmDepositTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Confirm deposit → 200")
        void confirmDeposit_shouldReturn200() throws Exception {
            DepositPaymentRequest request = new DepositPaymentRequest();
            request.setMethod(PaymentMethod.CASH);
            request.setAmount(new BigDecimal("1000000"));
            request.setReference("REF-001");

            when(contractService.confirmDepositPaymentForStaff(eq(1L), any(DepositPaymentRequest.class))).thenReturn(sampleContract);
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/contracts/1/deposit/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Xác nhận thanh toán tiền cọc thành công"));
        }
    }

    // =========================================================
    // 8. POST /api/contracts/{id}/deposit/momo/initiate
    // =========================================================
    @Nested
    @DisplayName("POST /api/contracts/{id}/deposit/momo/initiate")
    class InitiateDepositMomoTests {

        @Test
        @WithMockUser(username = "tenant_test", roles = "TENANT")
        @DisplayName("✅ TENANT initiate deposit momo → 200 + payUrl")
        void initiateDepositMomo_asTenant_shouldReturn200() throws Exception {
            Contract contract = Contract.builder()
                    .id(1L)
                    .status(ContractStatus.SIGNED_PENDING_DEPOSIT)
                    .deposit(new BigDecimal("1000000"))
                    .build();

            when(contractRepository.findByIdAndTenant_Username(1L, "tenant_test"))
                    .thenReturn(Optional.of(contract));

            CreateMomoResponse momoResponse = CreateMomoResponse.builder()
                    .payUrl("https://momo.test/pay")
                    .orderId("DEP-1")
                    .build();
            when(momoService.createATMPayment(anyLong(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(momoResponse);

            mockMvc.perform(post("/api/contracts/1/deposit/momo/initiate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new DepositMomoInitiateRequest())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("MoMo initiated"))
                    .andExpect(jsonPath("$.data.payUrl").value("https://momo.test/pay"));
        }

        @Test
        @WithMockUser(username = "tenant_test", roles = "TENANT")
        @DisplayName("❌ TENANT contract not found → 403")
        void initiateDepositMomo_tenantNoContract_shouldReturn403() throws Exception {
            when(contractRepository.findByIdAndTenant_Username(1L, "tenant_test"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/api/contracts/1/deposit/momo/initiate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new DepositMomoInitiateRequest())))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.statusCode").value(403));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("❌ Contract status invalid → 400")
        void initiateDepositMomo_invalidStatus_shouldReturn400() throws Exception {
            Contract contract = Contract.builder()
                    .id(1L)
                    .status(ContractStatus.ACTIVE)
                    .deposit(new BigDecimal("1000000"))
                    .build();

            when(contractService.getContractForStaff(1L)).thenReturn(contract);

            mockMvc.perform(post("/api/contracts/1/deposit/momo/initiate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new DepositMomoInitiateRequest())))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    // =========================================================
    // 9. GET /api/contracts/my-contracts
    // =========================================================
    @Nested
    @DisplayName("GET /api/contracts/my-contracts")
    class GetMyContractsTests {

        @Test
        @WithMockUser(username = "tenant_test", roles = "TENANT")
        @DisplayName("✅ Tenant get my-contracts → 200 + list")
        void getMyContracts_shouldReturn200() throws Exception {
            Tenant tenant = new Tenant();
            tenant.setId(5L);

            when(tenantService.findByUsername("tenant_test")).thenReturn(Optional.of(tenant));
            when(contractService.findByTenantId(5L)).thenReturn(List.of(sampleContract));
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/contracts/my-contracts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }
    }

    // =========================================================
    // 10. GET /api/contracts/my-contracts/paged
    // =========================================================
    @Nested
    @DisplayName("GET /api/contracts/my-contracts/paged")
    class GetMyContractsPagedTests {

        @Test
        @WithMockUser(username = "tenant_test", roles = "TENANT")
        @DisplayName("✅ Tenant get my-contracts/paged → 200 + page")
        void getMyContractsPaged_shouldReturn200() throws Exception {
            Tenant tenant = new Tenant();
            tenant.setId(5L);

            when(tenantService.findByUsername("tenant_test")).thenReturn(Optional.of(tenant));
            Page<Contract> page = new PageImpl<>(List.of(sampleContract));
            when(contractService.findByTenantId(eq(5L), any())).thenReturn(page);
            when(contractMapper.toResponse(sampleContract)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/contracts/my-contracts/paged")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].id").value(1));
        }
    }

    // =========================================================
    // 11. POST /api/contracts/{id}/checkout-request
    // =========================================================
    @Nested
    @DisplayName("POST /api/contracts/{id}/checkout-request")
    class SubmitCheckoutRequestTests {

        @Test
        @WithMockUser(username = "tenant_test", roles = "TENANT")
        @DisplayName("✅ Submit checkout request → 201")
        void submitCheckoutRequest_shouldReturn201() throws Exception {
            CheckoutRequestResponse resp = new CheckoutRequestResponse();
            resp.setId(1L);
            resp.setContractId(1L);
            resp.setStatus("PENDING");

            when(checkoutService.submitCheckoutRequest(eq(1L), eq("tenant_test"), any(CheckoutRequestDto.class)))
                    .thenReturn(resp);

            CheckoutRequestDto req = new CheckoutRequestDto();
            req.setReason("Muon tra phong");

            mockMvc.perform(post("/api/contracts/1/checkout-request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }
    }

    // =========================================================
    // 12. PUT /api/contracts/checkout-requests/{requestId}/approve
    // =========================================================
    @Nested
    @DisplayName("PUT /api/contracts/checkout-requests/{requestId}/approve")
    class ApproveCheckoutRequestTests {

        @Test
        @WithMockUser(username = "manager", roles = "MANAGER")
        @DisplayName("✅ Approve checkout request → 200")
        void approveCheckoutRequest_shouldReturn200() throws Exception {
            CheckoutRequestResponse resp = new CheckoutRequestResponse();
            resp.setId(2L);
            resp.setStatus("APPROVED");

            when(checkoutService.approveRequest(eq(2L), eq("manager"))).thenReturn(resp);

            mockMvc.perform(put("/api/contracts/checkout-requests/2/approve"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));
        }
    }

    // =========================================================
    // 13. POST /api/contracts/{id}/finalize-checkout
    // =========================================================
    @Nested
    @DisplayName("POST /api/contracts/{id}/finalize-checkout")
    class FinalizeCheckoutTests {

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        @DisplayName("✅ Finalize checkout → 200")
        void finalizeCheckout_shouldReturn200() throws Exception {
            doNothing().when(checkoutService).finalizeCheckout(1L, "admin");

            mockMvc.perform(post("/api/contracts/1/finalize-checkout"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Hoàn tất trả phòng thành công"));
        }
    }

    // =========================================================
    // 14. GET /api/contracts/{id}/download
    // =========================================================
    @Nested
    @DisplayName("GET /api/contracts/{id}/download")
    class DownloadContractTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ Download contract → 200 + content headers")
        void downloadContract_shouldReturn200() throws Exception {
            Path temp = Files.createTempFile("contract-test-", ".docx");
            Files.writeString(temp, "dummy");
            FileSystemResource resource = new FileSystemResource(temp.toFile());

            when(contractService.downloadContract(1L)).thenReturn(resource);

            mockMvc.perform(get("/api/contracts/1/download"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(temp.getFileName().toString())));

            Files.deleteIfExists(temp);
        }
    }
}
