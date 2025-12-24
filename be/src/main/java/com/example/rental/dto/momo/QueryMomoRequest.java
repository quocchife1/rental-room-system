package com.example.rental.dto.momo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueryMomoRequest {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private String lang;
    private String signature;
}
