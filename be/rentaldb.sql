-- =============================
-- BẢNG NGƯỜI DÙNG (cơ bản)
-- =============================
CREATE TABLE guest (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    status ENUM('ACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================
-- BẢNG THÔNG TIN NGƯỜI THUÊ
-- =============================
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_code VARCHAR(10) UNIQUE NOT NULL, -- T001, T002...
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(20),
    avatar_url VARCHAR(255),
    cccd VARCHAR(20) UNIQUE,        -- CCCD/CMND
    student_id VARCHAR(20),         -- MSSV
    university VARCHAR(100),
    address VARCHAR(255),
    status ENUM('ACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================
-- BẢNG THÔNG TIN ĐỐI TÁC
-- =============================
CREATE TABLE partners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_code VARCHAR(10) UNIQUE NOT NULL, -- DT001, DT002...
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    tax_code VARCHAR(50),
    contact_person VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(20),
    address VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================
-- BẢNG THÔNG TIN NHÂN VIÊN
-- =============================
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(10) UNIQUE NOT NULL, -- NV001, NV002...
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(20),
    branch_id BIGINT,
    position ENUM('MANAGER', 'ACCOUNTANT', 'RECEPTIONIST', 'MAINTENANCE', 'SECURITY', 'ADMIN'),
    salary DECIMAL(12,2),
    hire_date DATE,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE SET NULL
);

-- =============================
-- BẢNG CƠ SỞ (CHI NHÁNH)
-- =============================
CREATE TABLE branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20)
);

-- =============================
-- BẢNG PHÒNG CHO THUÊ
-- =============================
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_code VARCHAR(20) UNIQUE NOT NULL, -- CN01-R101-1
    branch_id BIGINT NOT NULL,
    room_number VARCHAR(100),
    area DECIMAL(5,2),
    price DECIMAL(12,2) NOT NULL,
    status ENUM('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    description TEXT,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

-- BẢNG QUẢN LÝ ĐẶT PHÒNG
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_code VARCHAR(20) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    status ENUM('PENDING_CONFIRMATION', 'RESERVED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING_CONFIRMATION',
    reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiration_date TIMESTAMP, -- Thời gian hết hạn giữ phòng
    notes TEXT,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- =============================
-- BẢNG HỢP ĐỒNG THUÊ
-- =============================
CREATE TABLE contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    deposit DECIMAL(12,2),
    status ENUM('PENDING', 'ACTIVE', 'ENDED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- =============================
-- BẢNG HÓA ĐƠN
-- =============================
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    status ENUM('UNPAID', 'PAID', 'OVERDUE') DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

-- BẢNG ĐỊNH NGHĨA CÁC DỊCH VỤ
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_code VARCHAR(20) UNIQUE NOT NULL, -- VD: INTERNET, PARKING
    service_name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    unit VARCHAR(20) NOT NULL, -- VD: 'tháng', 'xe', 'lần'
    description TEXT
);

-- BẢNG GHI NHẬN DỊCH VỤ MÀ NGƯỜI THUÊ SỬ DỤNG
CREATE TABLE contract_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    start_date DATE NOT NULL,
    end_date DATE,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

-- BẢNG CHI TIẾT HÓA ĐƠN
CREATE TABLE invoice_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL, -- VD: "Tiền thuê phòng tháng 9", "Phí Internet"
    amount DECIMAL(12,2) NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- =============================
-- BẢNG YÊU CẦU BẢO TRÌ
-- =============================
CREATE TABLE maintenance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_code VARCHAR(20) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- =============================
-- BẢNG TIN ĐĂNG CỦA ĐỐI TÁC
-- =============================
CREATE TABLE partner_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(12,2),
    area DECIMAL(5,2),
    address VARCHAR(255) NOT NULL,
    post_type ENUM('NORMAL', 'PRIORITY') DEFAULT 'NORMAL',
    status ENUM('PENDING_APPROVAL', 'APPROVED', 'REJECTED') DEFAULT 'PENDING_APPROVAL';
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by BIGINT;
    approved_at TIMESTAMP;
    FOREIGN KEY (approved_by) REFERENCES employees(id);
    FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE CASCADE
);

-- =============================
-- BẢNG THANH TOÁN PHÍ ĐĂNG TIN
-- =============================
CREATE TABLE partner_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_code VARCHAR(20) UNIQUE NOT NULL,
    partner_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    paid_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    method ENUM('BANK_TRANSFER', 'CREDIT_CARD', 'MOMO', 'CASH'),
    FOREIGN KEY (partner_id) REFERENCES partners(id),
    FOREIGN KEY (post_id) REFERENCES partner_posts(id)
);

-- =========================
-- BẢNG HÌNH ẢNH
-- =========================
-- ẢNH CHO PHÒNG (nội bộ công ty)
CREATE TABLE room_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- ẢNH CHO TIN ĐĂNG ĐỐI TÁC
CREATE TABLE post_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES partner_posts(id)
);

