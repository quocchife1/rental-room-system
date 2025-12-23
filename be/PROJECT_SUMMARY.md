# Hệ Thống Quản Lý Cho Thuê Phòng Trọ - Tóm Tắt Dự Án

## 📋 Mô Tả Dự Án
Hệ thống quản lý cho thuê phòng trọ (Rental Management System) được xây dựng bằng Spring Boot 3.5.5 với Java 21. 
Hệ thống hỗ trợ quản lý phòng, hợp đồng, hoá đơn, bảo trì, dịch vụ và người dùng đa vai trò.

## 🏗️ Kiến Trúc Dự Án

### Công Nghệ Sử Dụng
- **Backend Framework**: Spring Boot 3.5.5
- **Java Version**: Java 21
- **Database**: MySQL 8.0+
- **Authentication**: JWT (jjwt 0.11.5)
- **ORM**: JPA/Hibernate
- **Mapping**: MapStruct 1.5.5.Final
- **API Documentation**: Springdoc OpenAPI 2.7.0
- **Build Tool**: Maven
- **PDF Generation**: iText7 8.0.2
- **Excel Processing**: Apache POI 5.2.5
- **Email**: Spring Mail

### Thư Mục Cấu Trúc

```
src/main/java/com/example/rental/
├── config/                      # Cấu hình ứng dụng
│   ├── AppConfig.java           # Spring Application Config
│   ├── CorsProperties.java       # CORS Configuration
│   ├── JwtProperties.java        # JWT Configuration
│   ├── SecurityConfig.java       # Spring Security Configuration
│   ├── SwaggerConfig.java        # Swagger/OpenAPI Configuration
│   └── WebConfig.java            # Web Configuration
├── controller/                  # REST Controllers (API Endpoints)
│   ├── AuthController.java       # Authentication & Registration
│   ├── TenantController.java     # Tenant Management
│   ├── RoomController.java       # Room Management
│   ├── ContractController.java   # Contract Management
│   ├── InvoiceController.java    # Invoice Management
│   ├── MaintenanceController.java # Maintenance Management
│   ├── EmployeeController.java   # Employee Management
│   ├── BranchController.java     # Branch Management
│   ├── PartnerController.java    # Partner Management
│   ├── RentalServiceController.java # Service Items Management
│   ├── FileUploadController.java # File Upload
│   └── ...                       # Các controller khác
├── dto/                         # Data Transfer Objects
│   ├── auth/
│   │   ├── AuthLoginRequest.java
│   │   ├── AuthRegisterRequest.java
│   │   ├── AuthResponse.java
│   │   ├── TenantRegisterRequest.java
│   │   ├── PartnerRegisterRequest.java
│   │   └── EmployeeRegisterRequest.java
│   ├── tenant/
│   │   ├── TenantResponse.java
│   │   └── TenantUpdateProfileRequest.java
│   ├── room/
│   │   ├── RoomRequest.java
│   │   ├── RoomResponse.java
│   │   └── RoomImageResponse.java
│   ├── contract/
│   │   ├── ContractCreateRequest.java
│   │   └── ContractResponse.java
│   ├── invoice/
│   │   ├── InvoiceRequest.java
│   │   ├── InvoiceResponse.java
│   │   ├── InvoiceDetailRequest.java
│   │   └── InvoiceDetailResponse.java
│   ├── branch/
│   │   ├── BranchRequest.java
│   │   └── BranchResponse.java
│   ├── employee/
│   │   └── EmployeeResponse.java
│   ├── partner/
│   │   ├── PartnerResponse.java
│   │   └── PartnerUpdateProfileRequest.java
│   ├── maintenance/
│   │   └── (DTOs bảo trì)
│   └── ApiResponseDto.java       # Generic Response Wrapper
├── entity/                      # JPA Entities
│   ├── BaseEntity.java           # Base class với createdAt, updatedAt
│   ├── Tenant.java               # Người thuê
│   ├── Room.java                 # Phòng
│   ├── Contract.java             # Hợp đồng
│   ├── Invoice.java              # Hoá đơn
│   ├── InvoiceDetail.java        # Chi tiết hoá đơn
│   ├── MaintenanceRequest.java   # Yêu cầu bảo trì
│   ├── Employees.java            # Nhân viên
│   ├── Branch.java               # Chi nhánh
│   ├── Partners.java             # Đối tác
│   ├── RentalServiceItem.java    # Mục dịch vụ
│   ├── ContractService.java      # Dịch vụ trong hợp đồng
│   ├── PartnerPayment.java       # Thanh toán đối tác
│   ├── Reservation.java          # Đặt phòng
│   ├── Guest.java                # Khách vãng lai
│   ├── UserStatus.java (enum)    # ACTIVE, BANNED
│   ├── RoomStatus.java (enum)    # AVAILABLE, RENTED, MAINTENANCE
│   ├── ContractStatus.java (enum)# PENDING, ACTIVE, TERMINATED
│   ├── InvoiceStatus.java (enum) # DRAFT, ISSUED, PAID, OVERDUE
│   ├── EmployeePosition.java (enum)
│   └── ...                       # Các entity khác
├── mapper/                      # MapStruct Mappers
│   ├── TenantMapper.java
│   ├── RoomMapper.java
│   ├── ContractMapper.java
│   ├── InvoiceMapper.java
│   ├── EmployeeMapper.java
│   ├── BranchMapper.java
│   ├── PartnerMapper.java
│   └── ...
├── repository/                  # Spring Data JPA Repositories
│   ├── TenantRepository.java
│   ├── RoomRepository.java
│   ├── ContractRepository.java
│   ├── InvoiceRepository.java
│   ├── BranchRepository.java
│   ├── EmployeeRepository.java
│   ├── PartnerRepository.java
│   └── ...
├── service/                     # Business Logic (Interfaces)
│   ├── AuthService.java
│   ├── TenantService.java
│   ├── RoomService.java
│   ├── ContractService.java
│   ├── InvoiceService.java
│   ├── MaintenanceRequestService.java
│   ├── EmployeeService.java
│   ├── BranchService.java
│   ├── PartnerService.java
│   ├── RentalServiceService.java
│   ├── EmailService.java
│   ├── CustomUserDetailsService.java
│   └── impl/                    # Service Implementations
│       ├── AuthServiceImpl.java
│       ├── TenantServiceImpl.java
│       ├── RoomServiceImpl.java
│       ├── ContractServiceImpl.java
│       ├── InvoiceServiceImpl.java
│       ├── MaintenanceRequestServiceImpl.java
│       ├── EmployeeServiceImpl.java
│       ├── BranchServiceImpl.java
│       ├── PartnerServiceImpl.java
│       ├── EmailServiceImpl.java
│       └── ...
├── security/                    # Security Components
│   ├── JwtAuthenticationFilter.java
│   ├── JwtProvider.java
│   └── CustomUserDetailsService.java
├── exception/                   # Custom Exceptions
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── GlobalExceptionHandler.java
├── utils/                       # Utility Classes
│   ├── ContractDocxGenerator.java # Sinh file Word hợp đồng
│   ├── FileStorageService.java
│   └── ...
├── scheduler/                   # Scheduled Tasks
├── seeder/                      # Data Seeding
└── RentalApplication.java       # Main Application Class

src/main/resources/
├── application.properties        # Application Configuration
└── templates/                   # HTML templates (if needed)

target/                         # Maven build output
└── generated-sources/          # MapStruct generated mappers
```

## 📚 Các Thực Thể (Entities) Chính

### 1. **Tenant (Người Thuê)**
```java
- id: Long (PK)
- username: String (unique)
- password: String
- fullName: String
- email: String (unique)
- phoneNumber: String
- cccd: String (unique) - Chứng chỉ căn cước
- studentId: String
- university: String
- address: String
- dob: String (ngày sinh)
- status: UserStatus (ACTIVE/BANNED)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### 2. **Room (Phòng)**
```java
- id: Long
- roomCode: String (unique)
- branch: Branch (FK)
- roomNumber: String
- area: BigDecimal
- price: BigDecimal
- status: RoomStatus (AVAILABLE/RENTED/MAINTENANCE)
- description: String
- images: List<RoomImage>
```

### 3. **Contract (Hợp Đồng)**
```java
- id: Long
- tenant: Tenant (FK)
- room: Room (FK)
- branchCode: String
- roomNumber: String
- startDate: LocalDate
- endDate: LocalDate
- deposit: BigDecimal
- status: ContractStatus (PENDING/ACTIVE/TERMINATED)
- contractFileUrl: String
- signedContractUrl: String
- services: List<ContractService>
```

### 4. **Invoice (Hoá Đơn)**
```java
- id: Long
- contract: Contract (FK)
- invoiceNumber: String (unique)
- issueDate: LocalDate
- dueDate: LocalDate
- totalAmount: BigDecimal
- paidAmount: BigDecimal
- status: InvoiceStatus
- details: List<InvoiceDetail>
```

## 🔐 Bảo Mật (Security)

### Authentication Flow
1. **Đăng Ký (Registration)**: `/api/auth/register/{guest|tenant|partner|employee}`
   - Validate dữ liệu với constraints
   - Hash password bằng BCryptPasswordEncoder
   - Lưu vào database

2. **Đăng Nhập (Login)**: `/api/auth/login`
   - Authenticate username/password
   - Sinh JWT token (hết hạn trong 24 giờ)
   - Trả về AccessToken

3. **Authorization**: JWT Filter
   - Kiểm tra token trong Header: `Authorization: Bearer <token>`
   - Validate và extract thông tin người dùng
   - Gắn vào SecurityContext

## 📡 REST API Endpoints

### Authentication
- `POST /api/auth/register/guest` - Đăng ký khách vãng lai
- `POST /api/auth/register/tenant` - Đăng ký người thuê
- `POST /api/auth/register/partner` - Đăng ký đối tác
- `POST /api/auth/register/employee` - Đăng ký nhân viên
- `POST /api/auth/login` - Đăng nhập

### Tenant Management
- `GET /api/management/tenants` - Lấy danh sách người thuê
- `GET /api/management/tenants/{id}` - Lấy chi tiết người thuê
- `PATCH /api/management/tenants/{id}` - Cập nhật hồ sơ người thuê

### Room Management
- `GET /api/rooms` - Lấy danh sách phòng
- `GET /api/rooms/{id}` - Lấy chi tiết phòng
- `GET /api/rooms/code/{roomCode}` - Lấy phòng theo mã
- `GET /api/rooms/branch/{branchCode}` - Lấy phòng theo chi nhánh
- `GET /api/rooms/status/{status}` - Lấy phòng theo trạng thái
- `POST /api/rooms` - Tạo phòng mới
- `PUT /api/rooms/{id}` - Cập nhật phòng
- `DELETE /api/rooms/{id}` - Xóa phòng

### Contract Management
- `GET /api/contracts` - Lấy danh sách hợp đồng
- `GET /api/contracts/{id}` - Lấy chi tiết hợp đồng
- `POST /api/contracts` - Tạo hợp đồng mới
- `POST /api/contracts/{id}/upload-signed` - Upload hợp đồng đã ký
- `GET /api/contracts/{id}/download` - Tải hợp đồng

### Invoice Management
- `GET /api/invoices` - Lấy danh sách hoá đơn
- `GET /api/invoices/{id}` - Lấy chi tiết hoá đơn
- `POST /api/invoices` - Tạo hoá đơn mới
- `PUT /api/invoices/{id}` - Cập nhật hoá đơn

### Branch Management
- `GET /api/branches` - Lấy danh sách chi nhánh
- `GET /api/branches/{id}` - Lấy chi tiết chi nhánh
- `POST /api/branches` - Tạo chi nhánh mới

## 📋 Quy Trình Chính

### 1. Tạo Hợp Đồng (Create Contract)
1. Người dùng gửi yêu cầu với thông tin hợp đồng
2. Service kiểm tra phòng có sẵn không
3. Tạo hoặc sử dụng Tenant hiện có
4. Sinh file Word hợp đồng tự động
5. Lưu hợp đồng vào DB với trạng thái PENDING
6. Trả về link tải file hợp đồng

### 2. Tạo Hoá Đơn (Create Invoice)
1. Kiểm tra hợp đồng tồn tại
2. Tính toán các dịch vụ (điện, nước, internet,...)
3. Tạo hoá đơn với chi tiết
4. Trả về hoá đơn với tổng tiền

### 3. Yêu Cầu Bảo Trì (Maintenance Request)
1. Người thuê tạo yêu cầu bảo trì
2. Upload ảnh chứng minh
3. Staff xử lý và cập nhật trạng thái
4. Gửi email thông báo hoàn thành

## ⚙️ Configuration

### Database Connection (application.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rentaldb
spring.datasource.username=root
spring.datasource.password=123456
spring.jpa.hibernate.ddl-auto=update
```

### JWT Configuration
```properties
jwt.secret=<your-secret-key>
jwt.expiration-ms=86400000 (24 hours)
```

### CORS Configuration
```properties
cors.allowedOrigins=http://localhost:3000,http://localhost:5173
cors.allowedMethods=GET,POST,PUT,DELETE,OPTIONS
cors.allowCredentials=true
```

### File Upload
```properties
file.upload-dir=uploads
spring.servlet.multipart.max-file-size=50MB
```

## 🚀 Chạy Ứng Dụng

### 1. Build Dự Án
```bash
mvn clean install
```

### 2. Chạy Ứng Dụng
```bash
mvn spring-boot:run
```
hoặc
```bash
java -jar target/rental-0.0.1-SNAPSHOT.jar
```

### 3. Truy Cập API
- API: `http://localhost:8080/api/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## 📝 Data Validation

Các DTO sử dụng Jakarta Validation:
- `@NotBlank` - Kiểm tra không để trống
- `@Email` - Validate email format
- `@Pattern` - Kiểm tra pattern (số điện thoại, mật khẩu)
- `@Min/@Max` - Kiểm tra giá trị min/max
- `@NotNull` - Không null

## 🔄 Error Handling

### Global Exception Handler
- Xử lý tất cả exceptions
- Trả về ApiResponseDto với thông tin lỗi
- HTTP Status phù hợp (400, 404, 500,...)

### Custom Exceptions
- `ResourceNotFoundException` - 404 Not Found
- `BadRequestException` - 400 Bad Request
- Tùy chỉnh theo yêu cầu

## 📊 Response Format

### Success Response
```json
{
  "statusCode": 200,
  "message": "Thành công",
  "data": {...},
  "timestamp": "2025-12-05T12:00:00",
  "path": "/api/..."
}
```

### Error Response
```json
{
  "statusCode": 400,
  "message": "Lỗi",
  "error": "Chi tiết lỗi",
  "timestamp": "2025-12-05T12:00:00",
  "path": "/api/..."
}
```

## 🛠️ Development Notes

### MapStruct Mapping
- Tự động generate mapper implementation
- Ignore field khi không cần mapping
- Hỗ trợ nested mapping
- Custom mapping methods khi cần

### Lombok Usage
- `@Data` - Tự động sinh getter/setter, equals, hashCode, toString
- `@Builder` - Builder pattern
- `@RequiredArgsConstructor` - Constructor với final fields
- `@EqualsAndHashCode(callSuper=true)` - Gọi superclass method

### Transaction Management
- `@Transactional` cho business logic
- Rollback tự động khi có exception
- Lazy loading cho relationships

## 📞 Support & Documentation

- Swagger/OpenAPI: `/swagger-ui.html`
- API Documentation: `/v3/api-docs`
- Database: MySQL 8.0+
- Java: JDK 21+

---

**Status**: ✅ Dự án hoàn thiện và biên dịch thành công

**Last Updated**: 2025-12-05
