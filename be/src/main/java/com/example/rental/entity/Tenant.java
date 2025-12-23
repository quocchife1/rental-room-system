package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên đăng nhập */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Mật khẩu */
    @Column(nullable = false, length = 255)
    private String password;

    /** Họ và tên */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /** Email */
    @Column(unique = true, length = 100)
    private String email;

    /** Số điện thoại */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /** Số CCCD/CMND */
    @Column(unique = true, length = 20)
    private String cccd;

    /** Mã số sinh viên */
    @Column(name = "student_id", length = 20)
    private String studentId;

    /** Tên trường đại học */
    @Column(length = 100)
    private String university;

    /** Địa chỉ */
    @Column(length = 255)
    private String address;

    /** Ngày sinh */
    @Column(name = "date_of_birth")
    private String dob;

    /** Trạng thái người dùng (ACTIVE/BANNED) */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status;
}
