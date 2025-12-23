package com.example.rental.dto.momo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMomoResponse {
    private String partnerCode; //
    private String orderId; //
    private String requestId; //
    private long amount; //
    private long responseTime;
    private String message;
    private int resultCode;
    private String payUrl;
}