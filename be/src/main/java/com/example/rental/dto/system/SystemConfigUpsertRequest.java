package com.example.rental.dto.system;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SystemConfigUpsertRequest {
    private BigDecimal electricPricePerUnit;
    private BigDecimal waterPricePerUnit;
    private BigDecimal lateFeePerDay;

    private String momoReceiverName;
    private String momoReceiverPhone;
    private String momoReceiverQrUrl;
}
