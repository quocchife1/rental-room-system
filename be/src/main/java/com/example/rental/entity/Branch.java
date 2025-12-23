package com.example.rental.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "branches")
public class Branch {

    /**
     * Khóa chính (Primary Key) của bảng.
     * Được tạo tự động bởi cơ sở dữ liệu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã chi nhánh (VD: CN01, CN02).
     * Bắt buộc (nullable = false) và là duy nhất (unique = true), độ dài tối đa 10 ký tự.
     */
    @Column(name = "branch_code", unique = true, nullable = true, length = 10)
    private String branchCode;

    /**
     * Tên chi nhánh.
     * Bắt buộc, độ dài tối đa 100 ký tự.
     */
    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;

    /**
     * Địa chỉ chi nhánh.
     * Bắt buộc, độ dài tối đa 255 ký tự.
     */
    @Column(nullable = false, length = 255)
    private String address;

    /**
     * Số điện thoại chi nhánh.
     * Độ dài tối đa 20 ký tự.
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Room> rooms;
}