package com.example.rental.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DepositMomoInitiateResponse {
    private String payUrl;
    private String orderId;
}
