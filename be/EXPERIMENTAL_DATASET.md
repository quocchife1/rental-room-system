# Dữ liệu thực nghiệm (mở rộng theo enum)

Ghi chú:
- Bộ dữ liệu này bám theo các `@Entity` trong `be/src/main/java/com/example/rental/entity` (Hibernate `ddl-auto=update`).
- `branches` và `rooms` có 2–5 dòng theo yêu cầu.
- Với các bảng có cột kiểu enum, đảm bảo **mỗi giá trị enum xuất hiện ít nhất 1 dòng**.
- Tên cột dùng đúng theo `@Column(name=...)` / `@JoinColumn(name=...)`; các cột không chỉ định `name` được hiểu theo convention `camelCase -> snake_case`.
- Thời gian dùng format `YYYY-MM-DD HH:MM:SS`.

## branches (2 dòng)
| id | branch_code | branch_name | address | phone_number |
|---:|---|---|---|---|
| 1 | CN01 | Chi nhánh Quận 1 | 123 Lê Lợi, Q1, TP.HCM | 0909000001 |
| 2 | CN02 | Chi nhánh Quận 7 | 456 Nguyễn Thị Thập, Q7, TP.HCM | 0909000002 |

## rooms (4 dòng: đủ enum RoomStatus)
| id | room_code | branch_code | branch_id | room_number | area | price | status | description |
|---:|---|---|---:|---|---:|---:|---|---|
| 1 | CN01-P101 | CN01 | 1 | P101 | 25.50 | 3500000.00 | AVAILABLE | Phòng có cửa sổ, nội thất cơ bản |
| 2 | CN01-P102 | CN01 | 1 | P102 | 22.00 | 3300000.00 | RESERVED | Đang được giữ phòng |
| 3 | CN02-P201 | CN02 | 2 | P201 | 28.00 | 3800000.00 | OCCUPIED | Đang có người thuê |
| 4 | CN02-P202 | CN02 | 2 | P202 | 20.00 | 3100000.00 | MAINTENANCE | Đang bảo trì |

## room_images
| id | room_id | image_url | is_thumbnail | created_at |
|---:|---:|---|---|---|
| 1 | 1 | uploads/rooms/CN01-P101-thumb.jpg | 1 | 2025-01-01 09:05:00 |
| 2 | 4 | uploads/rooms/CN02-P202-1.jpg | 0 | 2025-01-02 09:05:00 |

## tenants (2 dòng: đủ enum UserStatus)
| id | username | password | full_name | email | phone_number | cccd | student_id | university | address | date_of_birth | status | created_at | updated_at |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | tenant01 | $2a$10$demoHashTenant | Nguyễn Văn A | tenant01@example.com | 0901000001 | 012345678901 | SV001 | ĐH Quốc Gia | 10 Nguyễn Huệ, Q1 | 2000-01-15 | ACTIVE | 2025-01-01 09:00:00 | 2025-01-01 09:00:00 |
| 2 | tenant02 | $2a$10$demoHashTenant2 | Võ Thị E | tenant02@example.com | 0901000002 | 012345678902 | SV002 | ĐH Bách Khoa | 20 Trần Hưng Đạo, Q1 | 2001-02-20 | BANNED | 2025-01-01 09:10:00 | 2025-01-05 09:10:00 |

## employees (7 dòng: đủ enum EmployeePosition; đồng thời đủ enum UserStatus)
| id | employee_code | username | password | full_name | email | phone_number | branch_code | position | salary | hire_date | status | created_at | updated_at |
|---:|---|---|---|---|---|---|---|---|---:|---|---|---|---|
| 1 | NV01 | emp01 | $2a$10$demoHashEmp | Trần Thị B | emp01@example.com | 0902000001 | CN01 | MANAGER | 12000000.00 | 2024-12-01 | ACTIVE | 2025-01-01 08:50:00 | 2025-01-01 08:50:00 |
| 2 | NV02 | emp02 | $2a$10$demoHashEmp2 | Nguyễn Văn F | emp02@example.com | 0902000002 | CN01 | ACCOUNTANT | 11000000.00 | 2024-12-10 | ACTIVE | 2025-01-01 08:51:00 | 2025-01-01 08:51:00 |
| 3 | NV03 | emp03 | $2a$10$demoHashEmp3 | Lê Thị G | emp03@example.com | 0902000003 | CN02 | RECEPTIONIST | 9000000.00 | 2024-12-12 | ACTIVE | 2025-01-01 08:52:00 | 2025-01-01 08:52:00 |
| 4 | NV04 | emp04 | $2a$10$demoHashEmp4 | Phạm Văn H | emp04@example.com | 0902000004 | CN02 | MAINTENANCE | 9500000.00 | 2024-12-15 | ACTIVE | 2025-01-01 08:53:00 | 2025-01-01 08:53:00 |
| 5 | NV05 | emp05 | $2a$10$demoHashEmp5 | Trần Văn I | emp05@example.com | 0902000005 | CN01 | SECURITY | 8500000.00 | 2024-12-18 | ACTIVE | 2025-01-01 08:54:00 | 2025-01-01 08:54:00 |
| 6 | NV06 | emp06 | $2a$10$demoHashEmp6 | Nguyễn Thị K | emp06@example.com | 0902000006 | CN01 | DIRECTOR | 20000000.00 | 2024-11-01 | ACTIVE | 2025-01-01 08:55:00 | 2025-01-01 08:55:00 |
| 7 | NV07 | emp07 | $2a$10$demoHashEmp7 | Võ Văn L | emp07@example.com | 0902000007 | CN02 | ADMIN | 18000000.00 | 2024-11-15 | BANNED | 2025-01-01 08:56:00 | 2025-01-10 08:56:00 |

## guest (2 dòng: đủ enum UserStatus)
| id | username | password | full_name | email | phone_number | dob | status | created_at | updated_at |
|---:|---|---|---|---|---|---|---|---|---|
| 1 | guest01 | $2a$10$demoHashGuest | Lê Văn C | guest01@example.com | 0903000001 | 2002-05-20 | ACTIVE | 2025-01-01 08:40:00 | 2025-01-01 08:40:00 |
| 2 | guest02 | $2a$10$demoHashGuest2 | Hồ Thị M | guest02@example.com | 0903000002 | 2003-06-10 | BANNED | 2025-01-01 08:41:00 | 2025-01-08 08:41:00 |

## reservations (5 dòng: đủ enum ReservationStatus; đồng thời đủ enum VisitTimeSlot)
| id | reservation_code | tenant_id | room_id | status | reservation_date | visit_date | visit_slot | expiration_date | start_date | end_date | notes |
|---:|---|---:|---:|---|---|---|---|---|---|---|---|
| 1 | RES-0001 | 1 | 1 | PENDING_CONFIRMATION | 2025-01-01 10:00:00 | 2025-01-03 | MORNING | 2025-01-05 10:00:00 | 2025-01-10 00:00:00 | 2025-07-10 00:00:00 | Muốn xem phòng trước khi ký hợp đồng |
| 2 | RES-0002 | 1 | 2 | RESERVED | 2025-01-02 10:00:00 | 2025-01-04 | AFTERNOON | 2025-01-06 10:00:00 |  |  | Đã được giữ phòng |
| 3 | RES-0003 | 2 | 3 | CANCELLED | 2025-01-03 10:00:00 | 2025-01-06 | MORNING | 2025-01-07 10:00:00 |  |  | Khách hủy lịch |
| 4 | RES-0004 | 1 | 3 | COMPLETED | 2025-01-04 10:00:00 | 2025-01-07 | AFTERNOON | 2025-01-08 10:00:00 |  |  | Hoàn thành và chuyển sang hợp đồng |
| 5 | RES-0005 | 1 | 4 | NO_SHOW | 2025-01-05 10:00:00 | 2025-01-09 | MORNING | 2025-01-10 10:00:00 |  |  | Khách không đến |

## contracts (5 dòng: đủ enum ContractStatus; đồng thời đủ enum PaymentMethod cho deposit_payment_method)
| id | tenant_id | room_id | branch_code | room_number | start_date | end_date | deposit | deposit_payment_method | deposit_paid_date | deposit_payment_reference | deposit_invoice_url | deposit_receipt_url | end_reminder_sent | status | created_at | contract_file_url | signed_contract_url |
|---:|---:|---:|---|---|---|---|---:|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | 1 | CN01 | P101 | 2025-01-10 | 2025-07-10 | 1000000.00 |  |  |  |  |  | 0 | PENDING | 2025-01-01 10:30:00 | uploads/generated_contracts/contract-1.pdf |  |
| 2 | 1 | 2 | CN01 | P102 | 2025-01-10 | 2025-07-10 | 1000000.00 | MOMO | 2025-01-02 11:00:00 | MOMO-DEP-0002 | uploads/generated_contracts/contract-2.pdf | uploads/deposit_docs/receipt-2.png | 0 | SIGNED_PENDING_DEPOSIT | 2025-01-01 10:31:00 | uploads/generated_contracts/contract-2.pdf | uploads/contracts/signed-contract-2.pdf |
| 3 | 1 | 3 | CN02 | P201 | 2025-01-10 | 2025-07-10 | 1000000.00 | BANK_TRANSFER | 2025-01-02 12:00:00 | BANK-DEP-0003 | uploads/generated_contracts/contract-3.pdf | uploads/deposit_docs/receipt-3.png | 0 | ACTIVE | 2025-01-01 10:32:00 | uploads/generated_contracts/contract-3.pdf | uploads/contracts/signed-contract-3.pdf |
| 4 | 1 | 4 | CN02 | P202 | 2024-06-01 | 2024-12-01 | 1000000.00 | CASH | 2024-06-01 09:00:00 | CASH-DEP-0004 |  |  | 1 | ENDED | 2024-06-01 09:00:00 | uploads/generated_contracts/contract-4.pdf | uploads/contracts/signed-contract-4.pdf |
| 5 | 2 | 1 | CN01 | P101 | 2025-03-01 | 2025-04-01 | 500000.00 | CREDIT_CARD |  |  |  |  | 0 | CANCELLED | 2025-02-28 09:00:00 | uploads/generated_contracts/contract-5.pdf |  |

## services
| id | service_name | price | unit | description |
|---:|---|---:|---|---|
| 1 | Điện | 3500.00 | kWh | Tính theo chỉ số điện |

## contract_services
| id | contract_id | service_id | quantity | previous_reading | current_reading | start_date | end_date |
|---:|---:|---:|---:|---:|---:|---|---|
| 1 | 3 | 1 | 1 | 120.00 | 150.00 | 2025-01-10 | 2025-02-10 |

## service_bookings (3 dòng: đủ enum ServiceBookingStatus)
| id | contract_id | service_id | booking_date | start_time | end_time | status | cancel_reason | canceled_at | canceled_by | created_at |
|---:|---:|---:|---|---|---|---|---|---|---|---|
| 1 | 3 | 1 | 2025-01-15 | 09:00:00 | 10:00:00 | BOOKED |  |  |  | 2025-01-01 11:00:00 |
| 2 | 3 | 1 | 2025-01-20 | 09:00:00 | 10:00:00 | COMPLETED |  |  |  | 2025-01-10 11:00:00 |
| 3 | 3 | 1 | 2025-01-25 | 09:00:00 | 10:00:00 | CANCELED | Trùng lịch | 2025-01-24 18:00:00 | emp03 | 2025-01-15 11:00:00 |

## service_packages
| id | name | price | duration_days | description | is_active |
|---:|---|---:|---:|---|---|
| 1 | Gói vệ sinh cơ bản | 200000.00 | 30 | Vệ sinh phòng 1 lần/tháng | 1 |

## service_items
| id | code | name | description | unit_price | billing_frequency | active |
|---:|---|---|---|---:|---|---|
| 1 | SVC_ELEC | Tiền điện | Theo số kWh tiêu thụ | 3500.00 | MONTHLY | 1 |

## invoices (3 dòng: đủ enum InvoiceStatus)
| id | contract_id | amount | due_date | billing_year | billing_month | paid_date | paid_direct | payment_reference | status | created_at |
|---:|---:|---:|---|---:|---:|---|---|---|---|---|
| 1 | 3 | 3655000.00 | 2025-02-05 | 2025 | 2 |  | 0 |  | UNPAID | 2025-02-01 08:00:00 |
| 2 | 3 | 3655000.00 | 2025-01-05 | 2025 | 1 | 2025-01-04 | 1 | CASH-INV-0002 | PAID | 2025-01-01 08:00:00 |
| 3 | 3 | 3655000.00 | 2024-12-05 | 2024 | 12 |  | 0 |  | OVERDUE | 2024-12-01 08:00:00 |

## invoice_details
| id | invoice_id | description | quantity | unit_price | amount |
|---:|---:|---|---:|---:|---:|
| 1 | 1 | Tiền phòng tháng 02/2025 | 1 | 3500000.00 | 3500000.00 |
| 2 | 2 | Tiền phòng tháng 01/2025 | 1 | 3500000.00 | 3500000.00 |
| 3 | 3 | Tiền phòng tháng 12/2024 | 1 | 3500000.00 | 3500000.00 |

## payments
| id | invoice_id | amount | method | provider_ref | status | processed_by | created_at |
|---:|---:|---:|---|---|---|---|---|
| 1 | 1 | 3655000.00 | MOMO | MOMO-PAY-0001 | PENDING | emp02 | 2025-02-01 08:05:00 |

## maintenance_requests (3 dòng: đủ enum MaintenanceStatus)
| id | request_code | tenant_id | room_id | description | status | resolution | cost | technician_name | invoice_id | created_at | updated_at |
|---:|---|---:|---:|---|---|---|---:|---|---:|---|---|
| 1 | MR-0001 | 1 | 1 | Máy lạnh kêu to, cần kiểm tra | PENDING |  |  |  |  | 2025-01-20 09:00:00 | 2025-01-20 09:00:00 |
| 2 | MR-0002 | 1 | 4 | Rò rỉ nước ở nhà vệ sinh | IN_PROGRESS | Đang thay ống | 150000.00 | emp04 |  | 2025-01-21 09:00:00 | 2025-01-22 09:00:00 |
| 3 | MR-0003 | 1 | 3 | Thay bóng đèn hành lang | COMPLETED | Đã thay bóng đèn | 50000.00 | emp04 | 2 | 2025-01-10 09:00:00 | 2025-01-11 09:00:00 |

## maintenance_images
| id | image_url | maintenance_request_id |
|---:|---|---:|
| 1 | uploads/maintenance/mr-0002-1.jpg | 2 |

## partners (2 dòng: đủ enum UserStatus)
| id | partner_code | username | password | company_name | tax_code | contact_person | email | phone_number | address | status | created_at | updated_at |
|---:|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | DT01 | partner01 | $2a$10$demoHashPartner | Công ty ABC | 0312345678 | Phạm Văn D | partner01@example.com | 0904000001 | 100 Điện Biên Phủ, TP.HCM | ACTIVE | 2025-01-01 08:30:00 | 2025-01-01 08:30:00 |
| 2 | DT02 | partner02 | $2a$10$demoHashPartner2 | Công ty XYZ | 0312345679 | Nguyễn Văn N | partner02@example.com | 0904000002 | 200 Pasteur, TP.HCM | BANNED | 2025-01-01 08:31:00 | 2025-01-09 08:31:00 |

## partner_posts (4 dòng: đủ enum PostType + PostApprovalStatus)
| id | partner_id | order_id | payment_url | title | description | price | area | address | post_type | status | created_at | approved_by | approved_at | is_deleted | reject_reason | views |
|---:|---:|---|---|---|---|---:|---:|---|---|---|---|---:|---|---|---|---:|
| 1 | 1 | ORDER-0001 | https://test-payment.momo.vn/pay/ORDER-0001 | Tin thường | Mô tả tin thường | 3500000.00 | 25.50 | 123 Lê Lợi, Q1, TP.HCM | NORMAL | PENDING_PAYMENT | 2025-01-01 12:00:00 |  |  | 0 |  | 0 |
| 2 | 1 | ORDER-0002 | https://test-payment.momo.vn/pay/ORDER-0002 | Tin VIP1 | Mô tả tin VIP1 | 3600000.00 | 26.00 | 123 Lê Lợi, Q1, TP.HCM | VIP1 | PENDING_APPROVAL | 2025-01-01 12:10:00 |  |  | 0 |  | 3 |
| 3 | 1 | ORDER-0003 | https://test-payment.momo.vn/pay/ORDER-0003 | Tin VIP2 | Mô tả tin VIP2 | 3700000.00 | 27.00 | 456 Nguyễn Thị Thập, Q7, TP.HCM | VIP2 | APPROVED | 2025-01-01 12:20:00 | 1 | 2025-01-01 12:30:00 | 0 |  | 10 |
| 4 | 1 | ORDER-0004 | https://test-payment.momo.vn/pay/ORDER-0004 | Tin VIP3 | Mô tả tin VIP3 | 3800000.00 | 28.00 | 456 Nguyễn Thị Thập, Q7, TP.HCM | VIP3 | REJECTED | 2025-01-01 12:25:00 | 1 | 2025-01-01 12:35:00 | 0 | Thiếu ảnh minh họa | 1 |

## post_images
| id | post_id | image_url | is_thumbnail | created_at |
|---:|---:|---|---|---|
| 1 | 3 | uploads/partner-posts/post-3-thumb.jpg | 1 | 2025-01-01 12:21:00 |

## partner_payments (4 dòng: đủ enum PaymentMethod)
| id | payment_code | partner_id | post_id | amount | paid_date | method |
|---:|---|---:|---:|---:|---|---|
| 1 | PP-0001 | 1 | 1 | 50000.00 | 2025-01-01 12:02:00 | MOMO |
| 2 | PP-0002 | 1 | 2 | 50000.00 | 2025-01-01 12:12:00 | BANK_TRANSFER |
| 3 | PP-0003 | 1 | 3 | 80000.00 | 2025-01-01 12:22:00 | CREDIT_CARD |
| 4 | PP-0004 | 1 | 4 | 100000.00 | 2025-01-01 12:26:00 | CASH |

## checkout_requests (4 dòng: đủ enum CheckoutStatus)
| id | contract_id | tenant_id | status | reason | created_at |
|---:|---:|---:|---|---|---|
| 1 | 3 | 1 | PENDING | Kết thúc hợp đồng đúng hạn | 2025-07-05 09:00:00 |
| 2 | 3 | 1 | APPROVED | Đã duyệt trả phòng | 2025-07-06 09:00:00 |
| 3 | 3 | 1 | REJECTED | Thiếu thông tin | 2025-07-07 09:00:00 |
| 4 | 4 | 1 | COMPLETED | Đã hoàn tất trả phòng | 2024-12-02 09:00:00 |

## damage_reports (4 dòng: đủ enum DamageReportStatus)
| id | contract_id | checkout_request_id | inspector_id | description | damage_details | total_damage_cost | settlement_invoice_id | status | approver_id | approver_note | created_at | approved_at | updated_at |
|---:|---:|---:|---:|---|---|---:|---:|---|---:|---|---|---|---|
| 1 | 3 | 1 | 1 | Phòng sạch, có hư hỏng nhỏ | [{"item":"Bóng đèn","damage":"Cháy","cost":50000}] | 50000.00 |  | DRAFT |  |  | 2025-07-05 10:00:00 |  | 2025-07-05 10:00:00 |
| 2 | 3 | 2 | 1 | Đã gửi biên bản | [{"item":"Vòi nước","damage":"Rò rỉ","cost":80000}] | 80000.00 |  | SUBMITTED |  |  | 2025-07-06 10:00:00 |  | 2025-07-06 10:00:00 |
| 3 | 3 | 3 | 1 | Biên bản được duyệt | [{"item":"Khóa cửa","damage":"Hỏng","cost":120000}] | 120000.00 | 2 | APPROVED | 2 | Đồng ý trừ cọc | 2025-07-07 10:00:00 | 2025-07-07 11:00:00 | 2025-07-07 11:00:00 |
| 4 | 4 | 4 | 1 | Biên bản bị từ chối | [{"item":"Tường","damage":"Vết bẩn","cost":30000}] | 30000.00 |  | REJECTED | 2 | Thiếu ảnh chứng minh | 2024-12-02 10:00:00 | 2024-12-02 11:00:00 | 2024-12-02 11:00:00 |

## damage_images
| id | damage_report_id | image_url | description |
|---:|---:|---|---|
| 1 | 3 | uploads/damage/dr-3-1.jpg | Ảnh khóa cửa hỏng |

## audit_logs (mỗi AuditAction 1 dòng)
| id | actor_id | actor_role | action | target_type | target_id | description | old_value | new_value | ip_address | user_agent | branch_id | created_at | status | error_message |
|---:|---|---|---|---|---:|---|---|---|---|---|---:|---|---|---|
| 1 | 1:emp01 (MANAGER) | MANAGER | LOGIN_SUCCESS | AUTH | 0 | Đăng nhập thành công |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 08:59:00 | SUCCESS |  |
| 2 | system | SYSTEM | LOGIN_FAILED | AUTH | 0 | Đăng nhập thất bại |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 08:59:30 | FAILURE | Sai mật khẩu |
| 3 | 1:emp01 (MANAGER) | MANAGER | LOGOUT | AUTH | 0 | Đăng xuất |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 09:01:00 | SUCCESS |  |
| 4 | system | SYSTEM | REGISTER_GUEST | GUEST | 1 | Tạo tài khoản guest01 |  | {"guestId":1} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 08:40:10 | SUCCESS |  |
| 5 | system | SYSTEM | REGISTER_TENANT | TENANT | 1 | Tạo tài khoản tenant01 |  | {"tenantId":1} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 09:00:10 | SUCCESS |  |
| 6 | system | SYSTEM | REGISTER_PARTNER | PARTNER | 1 | Tạo tài khoản partner01 |  | {"partnerId":1} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 08:30:10 | SUCCESS |  |
| 7 | system | SYSTEM | REGISTER_EMPLOYEE | EMPLOYEE | 1 | Tạo tài khoản emp01 |  | {"employeeId":1} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 08:50:10 | SUCCESS |  |
| 8 | 1:emp01 (MANAGER) | MANAGER | CREATE_CONTRACT | CONTRACT | 3 | Tạo hợp đồng ACTIVE |  | {"contractId":3,"room":"CN02-P201"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-01 10:32:10 | SUCCESS |  |
| 9 | 1:emp01 (MANAGER) | MANAGER | UPDATE_CONTRACT | CONTRACT | 3 | Cập nhật hợp đồng | {"endDate":"2025-07-10"} | {"endDate":"2025-08-10"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-02-01 10:00:00 | SUCCESS |  |
| 10 | 1:emp01 (MANAGER) | MANAGER | EXTEND_CONTRACT | CONTRACT | 3 | Gia hạn hợp đồng |  | {"extended":true} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-02-01 10:01:00 | SUCCESS |  |
| 11 | 1:emp01 (MANAGER) | MANAGER | TERMINATE_CONTRACT | CONTRACT | 3 | Chấm dứt hợp đồng |  | {"status":"ENDED"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-07-10 10:00:00 | SUCCESS |  |
| 12 | 1:emp01 (MANAGER) | MANAGER | SIGN_CONTRACT | CONTRACT | 2 | Upload hợp đồng đã ký |  | {"signed":true} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 10:31:30 | SUCCESS |  |
| 13 | 2:emp02 (ACCOUNTANT) | ACCOUNTANT | CREATE_INVOICE | INVOICE | 1 | Tạo hóa đơn tháng 02 |  | {"invoiceId":1} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-02-01 08:00:10 | SUCCESS |  |
| 14 | 2:emp02 (ACCOUNTANT) | ACCOUNTANT | UPDATE_INVOICE | INVOICE | 1 | Cập nhật hóa đơn |  | {"paidDirect":false} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-02-01 08:01:00 | SUCCESS |  |
| 15 | 2:emp02 (ACCOUNTANT) | ACCOUNTANT | CONFIRM_PAYMENT | INVOICE | 2 | Xác nhận thanh toán tiền mặt |  | {"status":"PAID"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-04 09:00:00 | SUCCESS |  |
| 16 | 2:emp02 (ACCOUNTANT) | ACCOUNTANT | REJECT_PAYMENT | INVOICE | 1 | Từ chối thanh toán |  | {"rejected":true} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-02-02 09:00:00 | SUCCESS |  |
| 17 | 2:emp02 (ACCOUNTANT) | ACCOUNTANT | CANCEL_INVOICE | INVOICE | 3 | Hủy hóa đơn |  | {"canceled":true} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-02-03 09:00:00 | SUCCESS |  |
| 18 | 1:emp01 (MANAGER) | MANAGER | UPDATE_PRICE | ROOM | 1 | Điều chỉnh giá phòng | {"price":3500000} | {"price":3600000} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-15 09:00:00 | SUCCESS |  |
| 19 | 1:emp01 (MANAGER) | MANAGER | ADD_SERVICE | CONTRACT | 3 | Thêm dịch vụ điện |  | {"serviceId":1} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-10 09:00:00 | SUCCESS |  |
| 20 | 1:emp01 (MANAGER) | MANAGER | REMOVE_SERVICE | CONTRACT | 3 | Gỡ dịch vụ | {"serviceId":1} |  | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-02-10 09:00:00 | SUCCESS |  |
| 21 | 6:emp06 (DIRECTOR) | DIRECTOR | CREATE_TENANT | TENANT | 2 | Tạo tenant02 |  | {"tenantId":2} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 09:10:10 | SUCCESS |  |
| 22 | 6:emp06 (DIRECTOR) | DIRECTOR | UPDATE_TENANT | TENANT | 1 | Cập nhật tenant01 |  | {"phone":"0901000001"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-02 09:10:10 | SUCCESS |  |
| 23 | 6:emp06 (DIRECTOR) | DIRECTOR | BAN_TENANT | TENANT | 2 | Khóa tài khoản tenant02 | {"status":"ACTIVE"} | {"status":"BANNED"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-05 09:10:10 | SUCCESS |  |
| 24 | 6:emp06 (DIRECTOR) | DIRECTOR | UNBAN_TENANT | TENANT | 2 | Mở khóa tài khoản tenant02 | {"status":"BANNED"} | {"status":"ACTIVE"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-06 09:10:10 | SUCCESS |  |
| 25 | 6:emp06 (DIRECTOR) | DIRECTOR | CREATE_EMPLOYEE | EMPLOYEE | 2 | Tạo emp02 |  | {"employeeId":2} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 08:51:10 | SUCCESS |  |
| 26 | 6:emp06 (DIRECTOR) | DIRECTOR | UPDATE_EMPLOYEE | EMPLOYEE | 2 | Cập nhật emp02 |  | {"salary":11000000} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-02 08:51:10 | SUCCESS |  |
| 27 | 1:emp01 (MANAGER) | MANAGER | CREATE_PARTNER_POST | PARTNER_POST | 3 | Tạo tin VIP2 |  | {"postId":3} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 12:20:10 | SUCCESS |  |
| 28 | 1:emp01 (MANAGER) | MANAGER | APPROVE_PARTNER_POST | PARTNER_POST | 3 | Duyệt tin | {"status":"PENDING_APPROVAL"} | {"status":"APPROVED"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 12:30:10 | SUCCESS |  |
| 29 | 1:emp01 (MANAGER) | MANAGER | REJECT_PARTNER_POST | PARTNER_POST | 4 | Từ chối tin |  | {"status":"REJECTED"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 12:35:10 | SUCCESS |  |
| 30 | 1:emp01 (MANAGER) | MANAGER | UPDATE_PARTNER_POST | PARTNER_POST | 2 | Cập nhật nội dung tin |  | {"title":"Tin VIP1"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 12:15:10 | SUCCESS |  |
| 31 | 1:emp01 (MANAGER) | MANAGER | DELETE_PARTNER_POST | PARTNER_POST | 1 | Xóa mềm tin | {"isDeleted":false} | {"isDeleted":true} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-03 12:00:00 | SUCCESS |  |
| 32 | 1:emp01 (MANAGER) | MANAGER | CREATE_ROOM | ROOM | 1 | Tạo phòng mới |  | {"roomId":1} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 09:00:00 | SUCCESS |  |
| 33 | 1:emp01 (MANAGER) | MANAGER | UPDATE_ROOM | ROOM | 2 | Cập nhật phòng |  | {"status":"RESERVED"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-02 09:00:00 | SUCCESS |  |
| 34 | 1:emp01 (MANAGER) | MANAGER | CHANGE_ROOM_STATUS | ROOM | 4 | Chuyển trạng thái bảo trì | {"status":"AVAILABLE"} | {"status":"MAINTENANCE"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-02 09:01:00 | SUCCESS |  |
| 35 | 1:emp01 (MANAGER) | MANAGER | DELETE_ROOM | ROOM | 0 | Xóa phòng (demo) |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-03 09:01:00 | SUCCESS |  |
| 36 | 3:emp03 (RECEPTIONIST) | RECEPTIONIST | CREATE_RESERVATION | RESERVATION | 1 | Tạo đặt phòng |  | {"reservationId":1} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-01 10:00:10 | SUCCESS |  |
| 37 | 3:emp03 (RECEPTIONIST) | RECEPTIONIST | CONFIRM_RESERVATION | RESERVATION | 2 | Xác nhận giữ phòng |  | {"status":"RESERVED"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-02 10:05:10 | SUCCESS |  |
| 38 | 3:emp03 (RECEPTIONIST) | RECEPTIONIST | CANCEL_RESERVATION | RESERVATION | 3 | Hủy đặt phòng |  | {"status":"CANCELLED"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-03 10:05:10 | SUCCESS |  |
| 39 | 4:emp04 (MAINTENANCE) | MAINTENANCE | CREATE_MAINTENANCE_REQUEST | MAINTENANCE | 1 | Tạo yêu cầu bảo trì |  | {"requestId":1} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-20 09:00:10 | SUCCESS |  |
| 40 | 4:emp04 (MAINTENANCE) | MAINTENANCE | UPDATE_MAINTENANCE_STATUS | MAINTENANCE | 2 | Cập nhật trạng thái | {"status":"PENDING"} | {"status":"IN_PROGRESS"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-22 09:00:10 | SUCCESS |  |
| 41 | 4:emp04 (MAINTENANCE) | MAINTENANCE | COMPLETE_MAINTENANCE | MAINTENANCE | 3 | Hoàn tất bảo trì | {"status":"IN_PROGRESS"} | {"status":"COMPLETED"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-11 09:00:10 | SUCCESS |  |
| 42 | 6:emp06 (DIRECTOR) | DIRECTOR | ASSIGN_ROLE | EMPLOYEE | 3 | Gán quyền |  | {"role":"RECEPTIONIST"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-02 08:00:00 | SUCCESS |  |
| 43 | 6:emp06 (DIRECTOR) | DIRECTOR | REMOVE_ROLE | EMPLOYEE | 3 | Gỡ quyền | {"role":"RECEPTIONIST"} |  | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-01-03 08:00:00 | SUCCESS |  |
| 44 | 6:emp06 (DIRECTOR) | DIRECTOR | GRANT_PERMISSION | EMPLOYEE | 2 | Cấp quyền |  | {"perm":"VIEW_REPORTS"} | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-03 08:10:00 | SUCCESS |  |
| 45 | 6:emp06 (DIRECTOR) | DIRECTOR | REVOKE_PERMISSION | EMPLOYEE | 2 | Thu hồi quyền | {"perm":"VIEW_REPORTS"} |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-04 08:10:00 | SUCCESS |  |
| 46 | 6:emp06 (DIRECTOR) | DIRECTOR | MANUAL_ADJUSTMENT | SYSTEM | 0 | Điều chỉnh thủ công |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-04 09:00:00 | SUCCESS |  |
| 47 | system | SYSTEM | SYSTEM_AUTO_ACTION | SYSTEM | 0 | Tác vụ tự động |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-05 09:00:00 | SUCCESS |  |
| 48 | 6:emp06 (DIRECTOR) | DIRECTOR | BACKUP_DATA | SYSTEM | 0 | Sao lưu dữ liệu |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-06 09:00:00 | SUCCESS |  |
| 49 | 6:emp06 (DIRECTOR) | DIRECTOR | DELETE_DATA | SYSTEM | 0 | Xóa dữ liệu |  |  | 127.0.0.1 | Mozilla/5.0 | 1 | 2025-01-07 09:00:00 | SUCCESS |  |
| 50 | 1:emp01 (MANAGER) | MANAGER | SUBMIT_CHECKOUT_REQUEST | CHECKOUT | 1 | Gửi yêu cầu trả phòng |  | {"checkoutRequestId":1} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-07-05 09:00:10 | SUCCESS |  |
| 51 | 1:emp01 (MANAGER) | MANAGER | APPROVE_CHECKOUT | CHECKOUT | 2 | Duyệt trả phòng |  | {"status":"APPROVED"} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-07-06 09:00:10 | SUCCESS |  |
| 52 | 1:emp01 (MANAGER) | MANAGER | DAMAGE_ASSESSMENT | DAMAGE_REPORT | 3 | Lập biên bản hư hại |  | {"damageReportId":3} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-07-07 10:00:10 | SUCCESS |  |
| 53 | 2:emp02 (ACCOUNTANT) | ACCOUNTANT | FINAL_SETTLEMENT | DAMAGE_REPORT | 3 | Quyết toán cuối |  | {"settlementInvoiceId":2} | 127.0.0.1 | Mozilla/5.0 | 2 | 2025-07-07 12:00:10 | SUCCESS |  |

## system_config
| id | electric_price_per_unit | water_price_per_unit | late_fee_per_day | momo_receiver_name | momo_receiver_phone | momo_receiver_qr_url | updated_at |
|---:|---:|---:|---:|---|---|---|---|
| 1 | 3500.00 | 15000.00 | 20000.00 | NHÀ TRỌ DEMO | 0909000001 | uploads/system/momo-qr.png | 2025-01-01 08:00:00 |
