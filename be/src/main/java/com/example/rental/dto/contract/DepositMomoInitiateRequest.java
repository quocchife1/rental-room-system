package com.example.rental.dto.contract;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DepositMomoInitiateRequest {
    /** Optional override amount; if null, use contract.deposit */
    private BigDecimal amount;

    /** Optional frontend return path (relative, starts with '/'). */
    private String returnPath;
}
