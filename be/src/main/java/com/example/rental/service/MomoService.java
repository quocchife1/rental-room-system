package com.example.rental.service;

import java.util.Map;
import com.example.rental.dto.momo.CreateMomoResponse;

public interface MomoService {
    CreateMomoResponse createATMPayment(long amount, String orderId);

    /**
     * Create a MoMo payment with custom order info/redirect and optional extraData.
     * This is used for flows other than partner-post payments (e.g. contract deposit).
     */
    CreateMomoResponse createATMPayment(long amount, String orderId, String orderInfo, String redirectUrl, String extraData);

    void handleMomoCallback(Map<String, Object> payload);
}