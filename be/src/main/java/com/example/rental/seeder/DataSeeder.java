package com.example.rental.seeder;

import com.example.rental.entity.*;
import com.example.rental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * DataSeeder - Khởi tạo dữ liệu mẫu cho hệ thống quản lý cho thuê phòng
 * Dữ liệu là những thông tin thực tế của người Việt
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final BranchRepository branchRepository;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final RentalServiceRepository rentalServiceRepository;
    private final GuestRepository guestRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicePackageRepository servicePackageRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerPostRepository partnerPostRepository;
    private final PostImageRepository postImageRepository;
    private final PartnerPaymentRepository partnerPaymentRepository;

    @Override
    public void run(String... args) {
        System.out.println("========== KHỞI ĐỘNG DATASEEDER ==========");

        try {
            seedBranches();
            seedRooms();
            seedRoomImages();
            seedServices();
            seedServicePackages();
            seedEmployees();
            seedGuests();
            seedPartnerData();

            System.out.println("========== DATASEEDER HOÀN THÀNH THÀNH CÔNG ==========");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi seed dữ liệu: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("DataSeeder failed", e);
        }
    }

    // ========== CHI NHÁNH ==========
    @Transactional
    private void seedBranches() {
        if (branchRepository.count() > 0) {
            System.out.println("✓ Chi nhánh đã tồn tại, bỏ qua...");
            return;
        }

        System.out.println("⚙ Đang tạo chi nhánh...");

        // Chi nhánh 1: TP.HCM - Quận 1
        Branch branch1 = Branch.builder()
                .branchName("Ký Túc Xá Quận 1 - TP.HCM")
                .address("123 Nguyễn Huệ, Quận 1, TP.HCM")
                .phoneNumber("0932123456")
                .build();
        branchRepository.save(branch1);

        // Chi nhánh 2: TP.HCM - Quận 3
        Branch branch2 = Branch.builder()
                .branchName("Ký Túc Xá Quận 3 - TP.HCM")
                .address("456 Bà Triệu, Quận 3, TP.HCM")
                .phoneNumber("0934567890")
                .build();
        branchRepository.save(branch2);

        // Chi nhánh 3: TP.HCM - Quận 10
        Branch branch3 = Branch.builder()
                .branchName("Ký Túc Xá Quận 10 - TP.HCM")
                .address("789 Đinh Bộ Lĩnh, Quận 10, TP.HCM")
                .phoneNumber("0936789012")
                .build();
        branchRepository.save(branch3);

        // Tạo mã chi nhánh tự động
        branchRepository.findAll().forEach(branch -> {
            if (branch.getBranchCode() == null) {
                branch.setBranchCode(generateBranchCode(branch.getId()));
                branchRepository.save(branch);
            }
        });

        System.out.println("✓ Tạo thành công " + branchRepository.count() + " chi nhánh");
    }

    // ========== PHÒNG ==========
    @Transactional
    private void seedRooms() {
        if (roomRepository.count() > 0) {
            System.out.println("✓ Phòng đã tồn tại, bỏ qua...");
            return;
        }

        System.out.println("⚙ Đang tạo phòng...");

        List<Branch> branches = branchRepository.findAll();
        if (branches.isEmpty()) {
            System.err.println("❌ Không có chi nhánh để tạo phòng!");
            return;
        }

        int roomCount = 0;

        // Chi nhánh 1: Quận 1
        for (int i = 1; i <= 10; i++) {
            Branch branch = branches.get(0);
            String roomNum = String.format("%02d", i);

            createRoom(branch, roomNum, BigDecimal.valueOf(20 + i),
                    BigDecimal.valueOf(3000000L + (i * 500000)),
                    "Phòng " + i + " - Có máy lạnh, cửa sổ thoáng mát");
            roomCount++;
        }

        // Chi nhánh 2: Quận 3
        for (int i = 1; i <= 8; i++) {
            Branch branch = branches.get(1);
            String roomNum = "2" + String.format("%02d", i);

            createRoom(branch, roomNum, BigDecimal.valueOf(18 + i),
                    BigDecimal.valueOf(2800000L + (i * 400000)),
                    "Phòng " + roomNum + " - Gần trường ĐH, an toàn");
            roomCount++;
        }

        // Chi nhánh 3: Quận 10
        for (int i = 1; i <= 12; i++) {
            Branch branch = branches.get(2);
            String roomNum = "3" + String.format("%02d", i);

            createRoom(branch, roomNum, BigDecimal.valueOf(22 + i),
                    BigDecimal.valueOf(3200000L + (i * 600000)),
                    "Phòng " + roomNum + " - View đẹp, tiện ích đầy đủ");
            roomCount++;
        }

        System.out.println("✓ Tạo thành công " + roomCount + " phòng");
    }

    private void createRoom(Branch branch, String roomNumber, BigDecimal area, BigDecimal price, String description) {
        String branchCode = branch.getBranchCode();
        String roomCode = branchCode + "-" + roomNumber;

        Room room = Room.builder()
                .roomCode(roomCode)
                .branchCode(branchCode)
                .roomNumber(roomNumber)
                .area(area)
                .price(price)
                .status(RoomStatus.AVAILABLE)
                .description(description)
                .branch(branch)
                .build();

        roomRepository.save(room);
    }

    // ========== HÌNH ẢNH PHÒNG (FIX LỖI 404) ==========
    @Transactional
    private void seedRoomImages() {
                boolean hasAnyImages = roomImageRepository.count() > 0;
                if (hasAnyImages) {
                        System.out.println("⚙ Đang cập nhật/bổ sung hình ảnh phòng (CN03 sẽ được thay bộ ảnh nếu đã tồn tại)...");
                } else {
                        System.out.println("⚙ Đang tạo hình ảnh phòng (Bộ ảnh ổn định)...");
                }

        List<Room> rooms = roomRepository.findAll();
        
        if (rooms.isEmpty()) {
            System.err.println("❌ Không có phòng để thêm hình ảnh!");
            return;
        }

        // --- BỘ ẢNH MỚI (Đã kiểm tra hoạt động) ---

        // Bộ 1: Quận 1 - Hiện đại (Modern / Minimalist)
        List<List<String>> modernImages = Arrays.asList(
            Arrays.asList(
                "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800", // Căn hộ hiện đại
                "https://images.unsplash.com/photo-1522770179533-24471fcdba45?w=800"  // Phòng trống sạch sẽ
            ),
            Arrays.asList(
                "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800", // Decor tối giản
                "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=800"  // Sofa hiện đại
            )
        );

        // Bộ 2: Quận 3 - Ấm cúng (Cozy / Warm light)
        List<List<String>> cozyImages = Arrays.asList(
            Arrays.asList(
                "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800", // Giường tầng/KTX
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800"  // Góc phòng ấm cúng
            ),
            Arrays.asList(
                "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?w=800", // Phòng ngủ nhỏ
                "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=800"  // Giường ngủ gọn gàng
            )
        );

        // Bộ 3: Quận 10 - Rộng rãi (Spacious / Luxury)
        List<List<String>> spaciousImages = Arrays.asList(
            Arrays.asList(
                                "https://picsum.photos/seed/cn03-spacious-1/1200/800",
                                "https://picsum.photos/seed/cn03-spacious-2/1200/800"
            ),
            Arrays.asList(
                                "https://picsum.photos/seed/cn03-spacious-3/1200/800",
                                "https://picsum.photos/seed/cn03-spacious-4/1200/800"
            )
        );

                int imageCount = 0;
                Random random = new Random();

                int updatedCount = 0;
                for (Room room : rooms) {
                        List<String> selectedSet;

                        // Tránh LazyInitializationException: không truy cập room.getBranch()
                        String branchCode = room.getBranchCode();
                        if ((branchCode == null || branchCode.isBlank()) && room.getRoomCode() != null) {
                                int dashIndex = room.getRoomCode().indexOf('-');
                                branchCode = dashIndex > 0 ? room.getRoomCode().substring(0, dashIndex) : room.getRoomCode();
                        }

                        int randomIndex = random.nextInt(2); // Random 0 hoặc 1

                        boolean isCN01 = branchCode != null && branchCode.contains("01");
                        boolean isCN02 = branchCode != null && branchCode.contains("02");
                        boolean isCN03 = branchCode != null && branchCode.contains("03");

                        // Chọn bộ ảnh theo mã chi nhánh (CN01/CN02/CN03)
                        if (isCN01) {
                                selectedSet = modernImages.get(randomIndex);
                        } else if (isCN02) {
                                selectedSet = cozyImages.get(randomIndex);
                        } else {
                                selectedSet = spaciousImages.get(randomIndex);
                        }

                        // Nếu đã có ảnh:
                        // - CN03: thay bộ ảnh (fix các ảnh lỗi như bạn report)
                        // - CN01/CN02: giữ nguyên, chỉ bổ sung nếu thiếu
                        if (hasAnyImages) {
                                java.util.List<RoomImage> existing = roomImageRepository.findByRoomId(room.getId());
                                if (!existing.isEmpty()) {
                                        if (isCN03) {
                                                RoomImage thumb = existing.stream()
                                                        .filter(RoomImage::getIsThumbnail)
                                                        .findFirst()
                                                        .orElse(null);

                                                RoomImage extra = existing.stream()
                                                        .filter(img -> !Boolean.TRUE.equals(img.getIsThumbnail()))
                                                        .findFirst()
                                                        .orElse(null);

                                                if (thumb == null || extra == null) {
                                                        roomImageRepository.deleteAll(existing);
                                                } else {
                                                        thumb.setImageUrl(selectedSet.get(0));
                                                        extra.setImageUrl(selectedSet.get(1));
                                                        roomImageRepository.save(thumb);
                                                        roomImageRepository.save(extra);
                                                        updatedCount += 2;
                                                        continue;
                                                }
                                        } else {
                                                // CN01/CN02: đã có ảnh -> không tạo thêm để tránh duplicate
                                                continue;
                                        }
                                }
                        }

            // --- LƯU 2 HÌNH ẢNH ---
            
            // Hình 1: Thumbnail (Ảnh chính)
            RoomImage img1 = RoomImage.builder()
                    .imageUrl(selectedSet.get(0))
                    .isThumbnail(true)
                    .room(room)
                    .build();
            roomImageRepository.save(img1);

            // Hình 2: Ảnh phụ
            RoomImage img2 = RoomImage.builder()
                    .imageUrl(selectedSet.get(1))
                    .isThumbnail(false)
                    .room(room)
                    .build();
            roomImageRepository.save(img2);

            imageCount += 2;
        }

                if (hasAnyImages) {
                        System.out.println("✓ Cập nhật " + updatedCount + " ảnh CN03, bổ sung " + imageCount + " ảnh thiếu (nếu có)");
                } else {
                        System.out.println("✓ Tạo thành công " + imageCount + " hình ảnh thật (Stable Links)");
                }
    }

    // ========== DỊCH VỤ CHO THUÊ ==========
    @Transactional
    private void seedServices() {
        if (rentalServiceRepository.count() > 0) {
                        // Ensure key services are aligned even when DB already has data.
                        normalizeServices();
                        System.out.println("✓ Dịch vụ đã tồn tại, đã chuẩn hóa danh mục dịch vụ...");
            return;
        }

        System.out.println("⚙ Đang tạo dịch vụ cho thuê...");

        // Điện
        RentalServiceItem electricity = RentalServiceItem.builder()
                .serviceName("Điện")
                .price(BigDecimal.valueOf(3500))
                .unit("kWh")
                .description("Điện công nghiệp, tính theo chỉ số")
                .build();

        // Nước
        RentalServiceItem water = RentalServiceItem.builder()
                .serviceName("Nước")
                .price(BigDecimal.valueOf(20000))
                .unit("m³")
                .description("Nước sạch, tính theo chỉ số")
                .build();

        // Internet
        RentalServiceItem internet = RentalServiceItem.builder()
                .serviceName("Internet")
                .price(BigDecimal.valueOf(150000))
                .unit("phòng/tháng")
                .description("Cáp quang tốc độ 100Mbps, 24/7")
                .build();

        // Giữ xe
        RentalServiceItem parking = RentalServiceItem.builder()
                .serviceName("Giữ xe máy")
                .price(BigDecimal.valueOf(50000))
                .unit("xe/tháng")
                .description("Giữ xe máy an toàn trong hầm xe")
                .build();

        // Vệ sinh
        RentalServiceItem cleaning = RentalServiceItem.builder()
                .serviceName("Vệ sinh")
                .price(BigDecimal.valueOf(100000))
                .unit("lần")
                .description("Vệ sinh theo lịch (Thứ 5 08:00-11:00)")
                .build();

        // Bảo vệ
        RentalServiceItem security = RentalServiceItem.builder()
                .serviceName("Bảo vệ 24/7")
                .price(BigDecimal.valueOf(80000))
                .unit("phòng/tháng")
                .description("Nhân viên bảo vệ 24 giờ")
                .build();

        // Nước nóng
        RentalServiceItem hotWater = RentalServiceItem.builder()
                .serviceName("Nước nóng")
                .price(BigDecimal.valueOf(100000))
                .unit("phòng/tháng")
                .description("Bình nước nóng sử dụng năng lượng mặt trời")
                .build();

        rentalServiceRepository.saveAll(List.of(electricity, water, internet, parking, cleaning, security, hotWater));

        System.out.println("✓ Tạo thành công " + rentalServiceRepository.count() + " dịch vụ");
    }

        private void normalizeServices() {
                // Ensure service "Vệ sinh" exists and is priced 100,000.
                rentalServiceRepository.findByServiceNameIgnoreCase("Vệ sinh")
                                .ifPresent(s -> {
                                        s.setPrice(BigDecimal.valueOf(100000));
                                        s.setUnit("lần");
                                        if (s.getDescription() == null || s.getDescription().isBlank()) {
                                                s.setDescription("Vệ sinh theo lịch (Thứ 5 08:00-11:00)");
                                        }
                                        rentalServiceRepository.save(s);
                                });

                // Backward-compatible rename: "Vệ sinh chung cư" -> "Vệ sinh" (keep record but align name & price)
                rentalServiceRepository.findByServiceNameIgnoreCase("Vệ sinh chung cư")
                                .ifPresent(s -> {
                                        s.setServiceName("Vệ sinh");
                                        s.setPrice(BigDecimal.valueOf(100000));
                                        s.setUnit("lần");
                                        s.setDescription("Vệ sinh theo lịch (Thứ 5 08:00-11:00)");
                                        rentalServiceRepository.save(s);
                                });

                // Ensure fixed security service exists
                if (rentalServiceRepository.findByServiceNameIgnoreCase("Bảo vệ 24/7").isEmpty()) {
                        RentalServiceItem security = RentalServiceItem.builder()
                                        .serviceName("Bảo vệ 24/7")
                                        .price(BigDecimal.valueOf(80000))
                                        .unit("phòng/tháng")
                                        .description("Nhân viên bảo vệ 24 giờ")
                                        .build();
                        rentalServiceRepository.save(security);
                }
        }

    // ========== GÓI DỊCH VỤ TIN ĐĂNG (PARTNER) ==========
    @Transactional
    private void seedServicePackages() {
        if (servicePackageRepository.count() > 0) {
            System.out.println("✓ Gói dịch vụ đã tồn tại, bỏ qua...");
            return;
        }

        System.out.println("⚙ Đang tạo gói dịch vụ tin đăng cho đối tác...");

        ServicePackage basic = ServicePackage.builder()
                .name("Gói thường")
                .price(new BigDecimal("50000"))
                .durationDays(7)
                .description("Hiển thị cơ bản trong 7 ngày")
                .isActive(true)
                .build();

        ServicePackage priority = ServicePackage.builder()
                .name("Gói ưu tiên")
                .price(new BigDecimal("150000"))
                .durationDays(7)
                .description("Ưu tiên hiển thị, đẩy tin trong 7 ngày")
                .isActive(true)
                .build();

        servicePackageRepository.saveAll(List.of(basic, priority));
        System.out.println("✓ Tạo thành công " + servicePackageRepository.count() + " gói dịch vụ");
    }

    // ========== KHÁCH ==========
    @Transactional
    private void seedGuests() {
                // Normalize existing guest usernames/passwords (so changes apply even when data already exists)
                String hashedPassword = passwordEncoder.encode("123456");
                normalizeGuestAccount("guest_john", "g1", hashedPassword);
                normalizeGuestAccount("guest_jane", "g2", hashedPassword);
                normalizeGuestAccount("guest_peter", "g3", hashedPassword);

        if (guestRepository.count() > 0) {
                        System.out.println("✓ Khách đã tồn tại, đã chuẩn hoá username ngắn + password 123456 (nếu có thể), bỏ qua tạo mới...");
            return;
        }

        System.out.println("⚙ Đang tạo khách...");

        // Khách 1
        Guest guest1 = Guest.builder()
                .username("g1")
                .password(hashedPassword)
                .fullName("Trần Minh Đức")
                .email("minh.duc@gmail.com")
                .phoneNumber("0971234567")
                .status(UserStatus.ACTIVE)
                .build();
        guestRepository.save(guest1);

        // Khách 2
        Guest guest2 = Guest.builder()
                .username("g2")
                .password(hashedPassword)
                .fullName("Lê Thị Hương")
                .email("huong.le@gmail.com")
                .phoneNumber("0972345678")
                .status(UserStatus.ACTIVE)
                .build();
        guestRepository.save(guest2);

        // Khách 3
        Guest guest3 = Guest.builder()
                .username("g3")
                .password(hashedPassword)
                .fullName("Phạm Quốc Bảo")
                .email("quoc.bao@gmail.com")
                .phoneNumber("0973456789")
                .status(UserStatus.ACTIVE)
                .build();
        guestRepository.save(guest3);

        System.out.println("✓ Tạo thành công " + guestRepository.count() + " khách");
    }

    // ========== NHÂN VIÊN ==========
    @Transactional
    private void seedEmployees() {
                // Normalize existing employee usernames/passwords (so changes apply even when data already exists)
                String hashedPassword = passwordEncoder.encode("123456");
                normalizeEmployeeAccount("admin", "admin", hashedPassword);
                normalizeEmployeeAccount("director", "dir", hashedPassword);
                normalizeEmployeeAccount("manager", "mgr", hashedPassword);
                normalizeEmployeeAccount("accountant", "acc", hashedPassword);
                normalizeEmployeeAccount("maintenance", "mt", hashedPassword);
                normalizeEmployeeAccount("receptionist", "rec", hashedPassword);

                System.out.println("⚙ Đang đảm bảo dữ liệu nhân viên (bổ sung nếu thiếu)...");

        List<Branch> branches = branchRepository.findAll();


        // Helper: create employee if missing
        java.util.function.BiConsumer<String, Employees> ensure = (username, employee) -> {
            Optional<Employees> existing = employeeRepository.findByUsername(username);
            if (existing.isPresent()) return;
            employeeRepository.save(employee);
            System.out.println("✓ Seeded employee -> username: " + username + " , password: 123456");
        };

        // Admin (gán tạm 1 chi nhánh bất kỳ để không null)
        ensure.accept("admin", Employees.builder()
                .username("admin")
                .employeeCode("EMP001")
                .password(hashedPassword)
                .fullName("Quản Trị Viên Hệ Thống")
                .email("admin@rentalsystem.com")
                .phoneNumber("0900000000")
                .position(EmployeePosition.ADMIN)
                .status(UserStatus.ACTIVE)
                .branch(branches.isEmpty() ? null : branches.get(0))
                .build());

        // Director (giám đốc) - quyền truy cập toàn hệ thống
        ensure.accept("dir", Employees.builder()
                .username("dir")
                .employeeCode("EMP002")
                .password(hashedPassword)
                .fullName("Giám Đốc Hệ Thống")
                .email("director@rentalsystem.com")
                .phoneNumber("0901111111")
                .position(EmployeePosition.DIRECTOR)
                .status(UserStatus.ACTIVE)
                .branch(null)
                .build());

        // Tài khoản chung (nếu cần)
        ensure.accept("acc", Employees.builder()
                .username("acc")
                .employeeCode("EMP003")
                .password(hashedPassword)
                .fullName("Trần Thị Kế Toán")
                .email("accountant@rentalsystem.com")
                .phoneNumber("0902222222")
                .position(EmployeePosition.ACCOUNTANT)
                .status(UserStatus.ACTIVE)
                .branch(branches.size() > 0 ? branches.get(0) : null)
                .build());

        ensure.accept("mt", Employees.builder()
                .username("mt")
                .employeeCode("EMP004")
                .password(hashedPassword)
                .fullName("Lê Văn Bảo Trì")
                .email("maintenance@rentalsystem.com")
                .phoneNumber("0903333333")
                .position(EmployeePosition.MAINTENANCE)
                .status(UserStatus.ACTIVE)
                .branch(branches.size() > 1 ? branches.get(1) : (branches.isEmpty() ? null : branches.get(0)))
                .build());

        // Manager/Receptionist cho từng chi nhánh (CN01/CN02/CN03)
        if (branches.size() > 0) {
            ensure.accept("mgr1", Employees.builder()
                    .username("mgr1")
                    .employeeCode("EMP101")
                    .password(hashedPassword)
                    .fullName("Quản Lý CN01")
                    .email("mgr1@rentalsystem.com")
                    .phoneNumber("0910000001")
                    .position(EmployeePosition.MANAGER)
                    .status(UserStatus.ACTIVE)
                    .branch(branches.get(0))
                    .build());

            ensure.accept("rec1", Employees.builder()
                    .username("rec1")
                    .employeeCode("EMP201")
                    .password(hashedPassword)
                    .fullName("Lễ Tân CN01")
                    .email("rec1@rentalsystem.com")
                    .phoneNumber("0920000001")
                    .position(EmployeePosition.RECEPTIONIST)
                    .status(UserStatus.ACTIVE)
                    .branch(branches.get(0))
                    .build());
        }

        if (branches.size() > 1) {
            ensure.accept("mgr2", Employees.builder()
                    .username("mgr2")
                    .employeeCode("EMP102")
                    .password(hashedPassword)
                    .fullName("Quản Lý CN02")
                    .email("mgr2@rentalsystem.com")
                    .phoneNumber("0910000002")
                    .position(EmployeePosition.MANAGER)
                    .status(UserStatus.ACTIVE)
                    .branch(branches.get(1))
                    .build());

            ensure.accept("rec2", Employees.builder()
                    .username("rec2")
                    .employeeCode("EMP202")
                    .password(hashedPassword)
                    .fullName("Lễ Tân CN02")
                    .email("rec2@rentalsystem.com")
                    .phoneNumber("0920000002")
                    .position(EmployeePosition.RECEPTIONIST)
                    .status(UserStatus.ACTIVE)
                    .branch(branches.get(1))
                    .build());
        }

        if (branches.size() > 2) {
            ensure.accept("mgr3", Employees.builder()
                    .username("mgr3")
                    .employeeCode("EMP103")
                    .password(hashedPassword)
                    .fullName("Quản Lý CN03")
                    .email("mgr3@rentalsystem.com")
                    .phoneNumber("0910000003")
                    .position(EmployeePosition.MANAGER)
                    .status(UserStatus.ACTIVE)
                    .branch(branches.get(2))
                    .build());

            // giữ tương thích với username ngắn cũ "rec" (nếu bạn đang dùng), nhưng vẫn bổ sung "rec3" rõ ràng
            ensure.accept("rec", Employees.builder()
                    .username("rec")
                    .employeeCode("EMP005")
                    .password(hashedPassword)
                    .fullName("Phạm Thị Lễ Tân")
                    .email("receptionist@rentalsystem.com")
                    .phoneNumber("0904444444")
                    .position(EmployeePosition.RECEPTIONIST)
                    .status(UserStatus.ACTIVE)
                    .branch(branches.get(2))
                    .build());

            ensure.accept("rec3", Employees.builder()
                    .username("rec3")
                    .employeeCode("EMP203")
                    .password(hashedPassword)
                    .fullName("Lễ Tân CN03")
                    .email("rec3@rentalsystem.com")
                    .phoneNumber("0920000003")
                    .position(EmployeePosition.RECEPTIONIST)
                    .status(UserStatus.ACTIVE)
                    .branch(branches.get(2))
                    .build());
        }

        System.out.println("✓ Tổng nhân viên hiện có: " + employeeRepository.count());
    }

    // ========== PARTNER & POSTS (Đã sửa lỗi) ==========
    @Transactional
    private void seedPartnerData() {
                // Normalize existing partner usernames/passwords (so changes apply even when data already exists)
                String hashedPassword = passwordEncoder.encode("123456");
                normalizePartnerAccount("partner_bds_thienan", "p1", hashedPassword);
                normalizePartnerAccount("partner_phongtro_sv", "p2", hashedPassword);

                boolean hasPartners = partnerRepository.count() > 0;
                boolean hasPosts = partnerPostRepository.count() > 0;

                if (hasPosts) {
                        System.out.println("✓ Bài đăng đối tác đã tồn tại, đã chuẩn hoá username ngắn + password 123456 (nếu có thể), bỏ qua tạo mới...");
                        return;
                }

                System.out.println("⚙ Đang tạo/đảm bảo dữ liệu Đối tác, Bài đăng, Thanh toán...");

        try {
            List<ServicePackage> packages = servicePackageRepository.findAll();
            if (packages.isEmpty()) {
                System.err.println("❌ Không có Gói dịch vụ, không thể tạo thanh toán cho đối tác.");
                return;
            }
            
            // Lấy các gói dịch vụ ra biến cục bộ để sử dụng sau này
            ServicePackage basicPkg = packages.stream()
                    .filter(p -> p.getName().toLowerCase().contains("thường"))
                    .findFirst()
                    .orElse(packages.get(0));
                    
            ServicePackage priorityPkg = packages.stream()
                    .filter(p -> p.getName().toLowerCase().contains("ưu tiên"))
                    .findFirst()
                    .orElse(packages.get(1));

        // --- TẠO/ĐẢM BẢO ĐỐI TÁC ---
        Partners partner1;
        Partners partner2;

        if (!hasPartners) {
            partner1 = Partners.builder()
                    .username("p1")
                    .password(hashedPassword)
                    .companyName("Bất động sản Thiên An")
                    .contactPerson("Nguyễn Thiên An")
                    .email("contact@thienanland.com")
                    .phoneNumber("0987654321")
                    .address("25 Lê Lợi, Quận 1, TP.HCM")
                    .status(UserStatus.ACTIVE)
                    .build();
            partnerRepository.save(partner1);

            partner2 = Partners.builder()
                    .username("p2")
                    .password(hashedPassword)
                    .companyName("Phòng Trọ Sinh Viên Giá Rẻ")
                    .contactPerson("Trần Minh Huy")
                    .email("support@phongtrosv.net")
                    .phoneNumber("0912345678")
                    .address("100 Dịch Vọng Hậu, Cầu Giấy, Hà Nội")
                    .status(UserStatus.ACTIVE)
                    .build();
            partnerRepository.save(partner2);

            System.out.println("✓ Tạo thành công 2 đối tác.");
        } else {
            java.util.List<Partners> partners = partnerRepository.findAll();
            partner1 = partners.stream().filter(p -> "p1".equals(p.getUsername())).findFirst().orElse(partners.get(0));
            partner2 = partners.stream().filter(p -> "p2".equals(p.getUsername())).findFirst()
                    .orElse(partners.size() > 1 ? partners.get(1) : partner1);
            System.out.println("✓ Đã có đối tác trong DB, sẽ dùng dữ liệu hiện có để tạo bài đăng/thanh toán (nếu thiếu).");
        }

                // --- TẠO BÀI ĐĂNG (Đã loại bỏ servicePackage trong builder) ---

        // Bài 1: Chờ duyệt (PENDING)
        PartnerPost post1 = PartnerPost.builder()
                .partner(partner1)
                .title("Cho thuê CHCC mini full nội thất Q.Bình Thạnh, 30m²")
                .description("Căn hộ dịch vụ mini mới xây, đầy đủ nội thất cao cấp. Gần Pearl Plaza, an ninh 24/7. Giờ giấc tự do, không chung chủ.")
                .address("123/45 Xô Viết Nghệ Tĩnh, P. 21, Q. Bình Thạnh, TP.HCM")
                .price(new BigDecimal("5500000"))
                .area(new BigDecimal("30.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.PENDING_APPROVAL)
                .build();
        partnerPostRepository.save(post1);

        // Bài 2: Đã duyệt (APPROVED)
        PartnerPost post2 = PartnerPost.builder()
                .partner(partner2)
                .title("Phòng trọ KTX giá rẻ cho SV ĐHQG, chỉ 1.5tr/tháng")
                .description("Phòng KTX máy lạnh, giường tầng cao cấp. Có khu vực học tập chung, bếp, máy giặt. Gần trạm xe buýt, an ninh tốt.")
                .address("1000/1A Kha Vạn Cân, P. Linh Trung, TP. Thủ Đức")
                .price(new BigDecimal("1500000"))
                .area(new BigDecimal("25.0"))
                .postType(PostType.VIP1)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(1))
                .build();
        partnerPostRepository.save(post2);

        // Bài 3: Bị từ chối (REJECTED)
        PartnerPost post3 = PartnerPost.builder()
                .partner(partner1)
                .title("Sang nhượng mặt bằng kinh doanh Q.1 giá CỰC RẺ")
                .description("Mặt bằng đẹp, giá sang nhượng chỉ thương lượng. Liên hệ ngay!!")
                .address("50 Nguyễn Trãi, P. Bến Thành, Q.1, TP.HCM")
                .price(new BigDecimal("10000000"))
                .area(new BigDecimal("50.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.REJECTED)
                .rejectReason("Thông tin giá không rõ ràng, hình ảnh không thực tế.")
                .build();
        partnerPostRepository.save(post3);

        // Bài 4: Phòng trọ Q.7 giá sinh viên
        PartnerPost post4 = PartnerPost.builder()
                .partner(partner2)
                .title("Phòng trọ Q.7 gần RMIT, có gác lửng 25m²")
                .description("Phòng rộng rãi, có gác lửng thoáng mát. Toilet riêng, bếp nấu ăn. Gần RMIT, Lotte Mart, siêu thị. An ninh tốt.")
                .address("218/3 Nguyễn Thị Thập, P. Tân Phú, Q.7, TP.HCM")
                .price(new BigDecimal("2800000"))
                .area(new BigDecimal("25.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(5))
                .build();
        partnerPostRepository.save(post4);

        // Bài 5: Căn hộ cao cấp Q.2
        PartnerPost post5 = PartnerPost.builder()
                .partner(partner1)
                .title("Cho thuê căn hộ studio Q.2 view sông, 40m²")
                .description("Căn hộ mới 100%, nội thất châu Âu sang trọng. View sông Sài Gòn tuyệt đẹp. Có hồ bơi, gym, siêu thị dưới tầng trệt.")
                .address("88 Đảo Kim Cương, P. Thạnh Mỹ Lợi, TP. Thủ Đức")
                .price(new BigDecimal("12000000"))
                .area(new BigDecimal("40.0"))
                .postType(PostType.VIP1)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(2))
                .build();
        partnerPostRepository.save(post5);

        // Bài 6: Phòng trọ Gò Vấp giá rẻ
        PartnerPost post6 = PartnerPost.builder()
                .partner(partner2)
                .title("Phòng trọ Gò Vấp có máy lạnh giá chỉ 2tr")
                .description("Phòng sạch sẽ, máy lạnh mới, tủ lạnh, giường nệm. Giờ giấc tự do. Gần chợ Gò Vấp, BV Gò Vấp. Điện 3k/kwh.")
                .address("456 Quang Trung, P.10, Q. Gò Vấp, TP.HCM")
                .price(new BigDecimal("2000000"))
                .area(new BigDecimal("20.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(7))
                .build();
        partnerPostRepository.save(post6);

        // Bài 7: Nhà nguyên căn Q.Tân Bình
        PartnerPost post7 = PartnerPost.builder()
                .partner(partner1)
                .title("Nhà nguyên căn 1 trệt 1 lầu Q.Tân Bình")
                .description("Nhà mới xây, 3 phòng ngủ, 3 WC. Có sân phơi, bếp rộng. Gần sân bay, Big C, trường học. Phù hợp gia đình.")
                .address("12/34 Cộng Hòa, P.13, Q. Tân Bình, TP.HCM")
                .price(new BigDecimal("15000000"))
                .area(new BigDecimal("80.0"))
                .postType(PostType.VIP1)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(3))
                .build();
        partnerPostRepository.save(post7);

        // Bài 8: Phòng trọ Hà Nội - Đống Đa
        PartnerPost post8 = PartnerPost.builder()
                .partner(partner2)
                .title("Phòng trọ Đống Đa gần ĐH Bách Khoa HN")
                .description("Phòng rộng 22m², nội thất cơ bản. Có thang máy, camera an ninh. Gần ĐH Bách Khoa, ĐH Xây Dựng. Cơm trưa 25k.")
                .address("56 Tạ Quang Bửu, P. Bách Khoa, Q. Hai Bà Trưng, Hà Nội")
                .price(new BigDecimal("2500000"))
                .area(new BigDecimal("22.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(4))
                .build();
        partnerPostRepository.save(post8);

        // Bài 9: Căn hộ Q.10 chờ duyệt
        PartnerPost post9 = PartnerPost.builder()
                .partner(partner1)
                .title("CHCC Q.10 gần Ngã Tư 3/2, đầy đủ tiện nghi")
                .description("Căn hộ 2PN, full nội thất. Có ban công thoáng mát, view công viên. Tầng 7, thang máy mới. Gần trường ĐH Y Dược.")
                .address("234 Ba Tháng Hai, P.12, Q.10, TP.HCM")
                .price(new BigDecimal("8500000"))
                .area(new BigDecimal("55.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.PENDING_APPROVAL)
                .build();
        partnerPostRepository.save(post9);

        // Bài 10: Phòng trọ Q.Phú Nhuận
        PartnerPost post10 = PartnerPost.builder()
                .partner(partner2)
                .title("Phòng trọ Phú Nhuận yên tĩnh, gần CV Gia Định")
                .description("Phòng sạch đẹp, có cửa sổ thoáng. Nội thất mới, giường nệm Dunlopillo. Khu dân trí cao, rất an toàn. Free nước.")
                .address("78 Phan Đăng Lưu, P.6, Q. Phú Nhuận, TP.HCM")
                .price(new BigDecimal("3200000"))
                .area(new BigDecimal("18.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(6))
                .build();
        partnerPostRepository.save(post10);

        // Bài 11: Chung cư mini Q.Bình Thạnh
        PartnerPost post11 = PartnerPost.builder()
                .partner(partner1)
                .title("CCMN Bình Thạnh cao cấp, có thang máy + bảo vệ")
                .description("Chung cư mini mới 100%. Thang máy, bảo vệ 24/7, camera. Full nội thất xịn. Gần Landmark 81, Metro. Không chung chủ.")
                .address("345 Điện Biên Phủ, P.15, Q. Bình Thạnh, TP.HCM")
                .price(new BigDecimal("6800000"))
                .area(new BigDecimal("35.0"))
                .postType(PostType.VIP1)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(1))
                .build();
        partnerPostRepository.save(post11);

        // Bài 12: Phòng trọ Q.12 giá rẻ
        PartnerPost post12 = PartnerPost.builder()
                .partner(partner2)
                .title("Phòng trọ Q.12 giá sinh viên, gần KCX Tân Bình")
                .description("Phòng sạch, có ban công phơi đồ. Khu an ninh, chủ nhà tốt. Gần KCX Tân Bình, Gò Vấp. Phù hợp công nhân, SV.")
                .address("890 Hà Huy Giáp, P. Thạnh Lộc, Q.12, TP.HCM")
                .price(new BigDecimal("1800000"))
                .area(new BigDecimal("16.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusDays(8))
                .build();
        partnerPostRepository.save(post12);


        // Bài 13: Nhà trọ Hà Nội - Cầu Giấy (Pending)
        PartnerPost post13 = PartnerPost.builder()
                .partner(partner2)
                .title("Nhà trọ Cầu Giấy gần BigC, có gác xép")
                .description("Phòng có gác lửng, toilet riêng. Có thể nấu ăn. Gần BigC Thăng Long, bến xe Mỹ Đình. Giờ giấc tự do.")
                .address("25 Trần Thái Tông, P. Dịch Vọng Hậu, Q. Cầu Giấy, Hà Nội")
                .price(new BigDecimal("3000000"))
                .area(new BigDecimal("28.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.PENDING_APPROVAL)
                .build();
        partnerPostRepository.save(post13);

        // Bài 14: Căn hộ dịch vụ Q.1 cao cấp
        PartnerPost post14 = PartnerPost.builder()
                .partner(partner1)
                .title("Căn hộ dịch vụ Q.1 mặt tiền Nguyễn Huệ, 50m²")
                .description("Căn hộ cao cấp ngay trung tâm. Nội thất 5 sao, có ban công. Dưới lầu là phố đi bộ Nguyễn Huệ. Phù hợp người nước ngoài, chuyên gia.")
                .address("88 Nguyễn Huệ, P. Bến Nghé, Q.1, TP.HCM")
                .price(new BigDecimal("18000000"))
                .area(new BigDecimal("50.0"))
                .postType(PostType.VIP1)
                .status(PostApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusHours(12))
                .build();
        partnerPostRepository.save(post14);

        // Bài 15: Phòng bị từ chối vì nội dung spam
        PartnerPost post15 = PartnerPost.builder()
                .partner(partner1)
                .title("PHÒNG RẺ NHẤT SÀI GÒN!!! GẤP GẤP!!!")
                .description("Giá cực sốc!!! Liên hệ ngay không hết!!! 0909xxx000")
                .address("Liên hệ để biết địa chỉ")
                .price(new BigDecimal("999000"))
                .area(new BigDecimal("10.0"))
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.REJECTED)
                .rejectReason("Tiêu đề spam, nội dung không rõ ràng, thiếu thông tin địa chỉ cụ thể.")
                .build();
        partnerPostRepository.save(post15);

        System.out.println("✓ Tạo thành công 15 bài đăng với các trạng thái khác nhau.");

        // --- TẠO HÌNH ẢNH BÀI ĐĂNG ---
        int imageCount = 0;
        
        // Post 1: CHCC mini Bình Thạnh (PENDING)
        postImageRepository.save(PostImage.builder().post(post1).imageUrl("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800").build());
        postImageRepository.save(PostImage.builder().post(post1).imageUrl("https://images.unsplash.com/photo-1616594039964-40891a909d99?w=800").build());
        imageCount += 2;

        // Post 2: KTX sinh viên (APPROVED - PRIORITY)
        postImageRepository.save(PostImage.builder().post(post2).imageUrl("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800").build());
        postImageRepository.save(PostImage.builder().post(post2).imageUrl("https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?w=800").build());
        imageCount += 2;

        // Post 3: Mặt bằng Q.1 (REJECTED)
        postImageRepository.save(PostImage.builder().post(post3).imageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=800").build());
        imageCount += 1;

        // Post 4: Phòng trọ Q.7 gác lửng (APPROVED - NORMAL)
        postImageRepository.save(PostImage.builder().post(post4).imageUrl("https://images.unsplash.com/photo-1540518614846-7eded433c457?w=800").build());
        postImageRepository.save(PostImage.builder().post(post4).imageUrl("https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800").build());
        postImageRepository.save(PostImage.builder().post(post4).imageUrl("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800").build());
        imageCount += 3;

        // Post 5: Studio Q.2 view sông (APPROVED - PRIORITY)
        postImageRepository.save(PostImage.builder().post(post5).imageUrl("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800").build());
        postImageRepository.save(PostImage.builder().post(post5).imageUrl("https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800").build());
        postImageRepository.save(PostImage.builder().post(post5).imageUrl("https://images.unsplash.com/photo-1502005229766-3c8ef95562fe?w=800").build());
        imageCount += 3;

        // Post 6: Phòng trọ Gò Vấp (APPROVED - NORMAL)
        postImageRepository.save(PostImage.builder().post(post6).imageUrl("https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=800").build());
        postImageRepository.save(PostImage.builder().post(post6).imageUrl("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=800").build());
        imageCount += 2;

        // Post 7: Nhà nguyên căn Tân Bình (APPROVED - PRIORITY)
        postImageRepository.save(PostImage.builder().post(post7).imageUrl("https://images.unsplash.com/photo-1560185127-6ed189bf02f4?w=800").build());
        postImageRepository.save(PostImage.builder().post(post7).imageUrl("https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800").build());
        postImageRepository.save(PostImage.builder().post(post7).imageUrl("https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800").build());
        imageCount += 3;

        // Post 8: Phòng trọ Hà Nội Đống Đa (APPROVED - NORMAL)
        postImageRepository.save(PostImage.builder().post(post8).imageUrl("https://images.unsplash.com/photo-1522770179533-24471fcdba45?w=800").build());
        postImageRepository.save(PostImage.builder().post(post8).imageUrl("https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800").build());
        imageCount += 2;

        // Post 9: CHCC Q.10 (PENDING)
        postImageRepository.save(PostImage.builder().post(post9).imageUrl("https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=800").build());
        postImageRepository.save(PostImage.builder().post(post9).imageUrl("https://images.unsplash.com/photo-1560448204-603b3fc33ddc?w=800").build());
        imageCount += 2;

        // Post 10: Phòng trọ Phú Nhuận (APPROVED - NORMAL)
        postImageRepository.save(PostImage.builder().post(post10).imageUrl("https://images.unsplash.com/photo-1554995207-c18c203602cb?w=800").build());
        postImageRepository.save(PostImage.builder().post(post10).imageUrl("https://images.unsplash.com/photo-1556909212-d5b604d0c90d?w=800").build());
        imageCount += 2;

        // Post 11: CCMN Bình Thạnh cao cấp (APPROVED - PRIORITY)
        postImageRepository.save(PostImage.builder().post(post11).imageUrl("https://images.unsplash.com/photo-1536376072261-38c75010e6c9?w=800").build());
        postImageRepository.save(PostImage.builder().post(post11).imageUrl("https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?w=800").build());
        postImageRepository.save(PostImage.builder().post(post11).imageUrl("https://images.unsplash.com/photo-1505693314120-0d443867891c?w=800").build());
        imageCount += 3;

        // Post 12: Phòng trọ Q.12 giá rẻ (APPROVED - NORMAL)
        postImageRepository.save(PostImage.builder().post(post12).imageUrl("https://images.unsplash.com/photo-1519643381401-22c77e60520e?w=800").build());
        postImageRepository.save(PostImage.builder().post(post12).imageUrl("https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=800").build());
        imageCount += 2;

        // Post 13: Nhà trọ Cầu Giấy (PENDING)
        postImageRepository.save(PostImage.builder().post(post13).imageUrl("https://images.unsplash.com/photo-1517487881594-2787fef5ebf7?w=800").build());
        postImageRepository.save(PostImage.builder().post(post13).imageUrl("https://images.unsplash.com/photo-1556020685-ae41abfc9365?w=800").build());
        imageCount += 2;

        // Post 14: Căn hộ dịch vụ Q.1 cao cấp (APPROVED - PRIORITY)
        postImageRepository.save(PostImage.builder().post(post14).imageUrl("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800").build());
        postImageRepository.save(PostImage.builder().post(post14).imageUrl("https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800").build());
        postImageRepository.save(PostImage.builder().post(post14).imageUrl("https://images.unsplash.com/photo-1515263487990-61b07816b324?w=800").build());
        imageCount += 3;

        // Post 15: Phòng spam (REJECTED)
        postImageRepository.save(PostImage.builder().post(post15).imageUrl("https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800").build());
        imageCount += 1;

        System.out.println("✓ Tạo thành công " + imageCount + " hình ảnh cho các bài đăng.");

        // --- TẠO THANH TOÁN (Sử dụng biến cục bộ thay vì post.getServicePackage()) ---
        int paymentCount = 0;
        long timestamp = System.currentTimeMillis();

        // Payment cho post2 (PRIORITY - APPROVED)
        PartnerPayment payment2 = PartnerPayment.builder()
                .partner(partner2)
                .post(post2)
                .paymentCode("PAY-" + timestamp++)
                .amount(priorityPkg.getPrice())
                .method(PaymentMethod.MOMO)
                .paidDate(LocalDateTime.now().minusDays(3))
                .build();
        partnerPaymentRepository.save(payment2);
        paymentCount++;

        // Payment cho post4 (NORMAL - APPROVED)
        PartnerPayment payment4 = PartnerPayment.builder()
                .partner(partner2)
                .post(post4)
                .paymentCode("PAY-" + timestamp++)
                .amount(basicPkg.getPrice())
                .method(PaymentMethod.BANK_TRANSFER)
                .paidDate(LocalDateTime.now().minusDays(5))
                .build();
        partnerPaymentRepository.save(payment4);
        paymentCount++;

        // Payment cho post5 (PRIORITY - APPROVED)
        PartnerPayment payment5 = PartnerPayment.builder()
                .partner(partner1)
                .post(post5)
                .paymentCode("PAY-" + timestamp++)
                .amount(priorityPkg.getPrice())
                .method(PaymentMethod.BANK_TRANSFER)
                .paidDate(LocalDateTime.now().minusDays(2))
                .build();
        partnerPaymentRepository.save(payment5);
        paymentCount++;

        // Payment cho post6 (NORMAL - APPROVED)
        PartnerPayment payment6 = PartnerPayment.builder()
                .partner(partner2)
                .post(post6)
                .paymentCode("PAY-" + timestamp++)
                .amount(basicPkg.getPrice())
                .method(PaymentMethod.MOMO)
                .paidDate(LocalDateTime.now().minusDays(7))
                .build();
        partnerPaymentRepository.save(payment6);
        paymentCount++;

        // Payment cho post7 (PRIORITY - APPROVED)
        PartnerPayment payment7 = PartnerPayment.builder()
                .partner(partner1)
                .post(post7)
                .paymentCode("PAY-" + timestamp++)
                .amount(priorityPkg.getPrice())
                .method(PaymentMethod.BANK_TRANSFER)
                .paidDate(LocalDateTime.now().minusDays(3))
                .build();
        partnerPaymentRepository.save(payment7);
        paymentCount++;

        // Payment cho post8 (NORMAL - APPROVED)
        PartnerPayment payment8 = PartnerPayment.builder()
                .partner(partner2)
                .post(post8)
                .paymentCode("PAY-" + timestamp++)
                .amount(basicPkg.getPrice())
                .method(PaymentMethod.MOMO)
                .paidDate(LocalDateTime.now().minusDays(4))
                .build();
        partnerPaymentRepository.save(payment8);
        paymentCount++;

        // Payment cho post10 (NORMAL - APPROVED)
        PartnerPayment payment10 = PartnerPayment.builder()
                .partner(partner2)
                .post(post10)
                .paymentCode("PAY-" + timestamp++)
                .amount(basicPkg.getPrice())
                .method(PaymentMethod.BANK_TRANSFER)
                .paidDate(LocalDateTime.now().minusDays(6))
                .build();
        partnerPaymentRepository.save(payment10);
        paymentCount++;

        // Payment cho post11 (PRIORITY - APPROVED)
        PartnerPayment payment11 = PartnerPayment.builder()
                .partner(partner1)
                .post(post11)
                .paymentCode("PAY-" + timestamp++)
                .amount(priorityPkg.getPrice())
                .method(PaymentMethod.BANK_TRANSFER)
                .paidDate(LocalDateTime.now().minusDays(1))
                .build();
        partnerPaymentRepository.save(payment11);
        paymentCount++;

        // Payment cho post12 (NORMAL - APPROVED)
        PartnerPayment payment12 = PartnerPayment.builder()
                .partner(partner2)
                .post(post12)
                .paymentCode("PAY-" + timestamp++)
                .amount(basicPkg.getPrice())
                .method(PaymentMethod.MOMO)
                .paidDate(LocalDateTime.now().minusDays(8))
                .build();
        partnerPaymentRepository.save(payment12);
        paymentCount++;

        // Payment cho post14 (PRIORITY - APPROVED)
        PartnerPayment payment14 = PartnerPayment.builder()
                .partner(partner1)
                .post(post14)
                .paymentCode("PAY-" + timestamp++)
                .amount(priorityPkg.getPrice())
                .method(PaymentMethod.BANK_TRANSFER)
                .paidDate(LocalDateTime.now().minusHours(12))
                .build();
        partnerPaymentRepository.save(payment14);
        paymentCount++;

        System.out.println("✓ Tạo thành công " + paymentCount + " giao dịch thanh toán cho đối tác.");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi seed dữ liệu đối tác: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ========== HELPER ==========
    private String generateBranchCode(Long id) {
        return String.format("CN%02d", id);
    }

        private void normalizeGuestAccount(String oldUsername, String newUsername, String hashedPassword) {
                Optional<Guest> newAccount = guestRepository.findByUsername(newUsername);
                newAccount.ifPresent(g -> {
                        g.setPassword(hashedPassword);
                        guestRepository.save(g);
                });

                Optional<Guest> oldAccount = guestRepository.findByUsername(oldUsername);
                oldAccount.ifPresent(g -> {
                        g.setPassword(hashedPassword);
                        if (!oldUsername.equals(newUsername) && newAccount.isEmpty()) {
                                g.setUsername(newUsername);
                        }
                        guestRepository.save(g);
                });
        }

        private void normalizeEmployeeAccount(String oldUsername, String newUsername, String hashedPassword) {
                Optional<Employees> newAccount = employeeRepository.findByUsername(newUsername);
                newAccount.ifPresent(e -> {
                        e.setPassword(hashedPassword);
                        employeeRepository.save(e);
                });

                Optional<Employees> oldAccount = employeeRepository.findByUsername(oldUsername);
                oldAccount.ifPresent(e -> {
                        e.setPassword(hashedPassword);
                        if (!oldUsername.equals(newUsername) && newAccount.isEmpty()) {
                                e.setUsername(newUsername);
                        }
                        employeeRepository.save(e);
                });
        }

        private void normalizePartnerAccount(String oldUsername, String newUsername, String hashedPassword) {
                Optional<Partners> newAccount = partnerRepository.findByUsername(newUsername);
                newAccount.ifPresent(p -> {
                        p.setPassword(hashedPassword);
                        partnerRepository.save(p);
                });

                Optional<Partners> oldAccount = partnerRepository.findByUsername(oldUsername);
                oldAccount.ifPresent(p -> {
                        p.setPassword(hashedPassword);
                        if (!oldUsername.equals(newUsername) && newAccount.isEmpty()) {
                                p.setUsername(newUsername);
                        }
                        partnerRepository.save(p);
                });
        }
}