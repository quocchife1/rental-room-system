package com.example.rental.dto.contract;

import com.example.rental.entity.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositPaymentRequest {
    // CASH hoặc BANK_TRANSFER
    private PaymentMethod method;

    // Cho phép nhập lại số tiền cọc thực nhận nếu cần; nếu null sẽ dùng contract.deposit
    private BigDecimal amount;

    // Mã giao dịch / nội dung chuyển khoản / ghi chú
    private String reference;
}
