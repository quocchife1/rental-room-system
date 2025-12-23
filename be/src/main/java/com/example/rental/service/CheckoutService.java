package com.example.rental.service;

import com.example.rental.dto.checkout.CheckoutRequestDto;
import com.example.rental.dto.checkout.CheckoutRequestResponse;

public interface CheckoutService {
    CheckoutRequestResponse submitCheckoutRequest(Long contractId, String username, CheckoutRequestDto request);
    CheckoutRequestResponse approveRequest(Long requestId, String approverUsername);
    void finalizeCheckout(Long contractId, String operatorUsername);
}
