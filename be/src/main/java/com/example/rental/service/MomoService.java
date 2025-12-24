package com.example.rental.service;

import java.util.Map;
import com.example.rental.dto.momo.CreateMomoResponse;
import com.example.rental.dto.momo.QueryMomoResponse;

public interface MomoService {
    CreateMomoResponse createATMPayment(long amount, String orderId);

    /**
     * Create a MoMo payment with custom order info/redirect and optional extraData.
     * This is used for flows other than partner-post payments (e.g. contract deposit).
     */
    CreateMomoResponse createATMPayment(long amount, String orderId, String orderInfo, String redirectUrl, String extraData);

    void handleMomoCallback(Map<String, Object> payload);

    /**
     * Query MoMo transaction status by orderId.
     */
    QueryMomoResponse queryTransaction(String orderId);

    /**
     * Handle MoMo browser return (no-ngrok friendly): will route by orderId prefix.
     */
    boolean handleMomoReturn(String orderId);

    /**
     * For local/dev flows without public IPN: user is redirected back to backend,
     * backend queries MoMo, then marks invoice paid if successful.
     */
    boolean handleMomoReturnForInvoice(String orderId);

    boolean handleMomoReturnForContractDeposit(String orderId);
}