package com.example.rental.service;

import com.example.rental.dto.payment.PaymentRequest;
import com.example.rental.dto.payment.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    /**
     * Initiate a third-party payment (MoMo) and return a redirect URL.
     */
    String initiateMoMo(PaymentRequest request, String returnUrl);
}
