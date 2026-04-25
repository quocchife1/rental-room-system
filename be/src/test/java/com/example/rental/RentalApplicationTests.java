package com.example.rental;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test – chỉ kiểm tra Spring Context có load thành công không.
 * Profile "test" dùng H2 in-memory, không cần MySQL thật.
 */
@SpringBootTest
@ActiveProfiles("test")
class RentalApplicationTests {

    /**
     * Context Load Test:
     * Nếu không có dependency nào bị thiếu / cấu hình sai,
     * Spring Boot sẽ khởi động thành công và test này PASS.
     */
    @Test
    void contextLoads() {
        // Không cần assert gì – nếu context load fail thì test sẽ tự fail
    }
}
