package com.example.rental.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CodeGenerator {

    /**
     * Generate a unique code with prefix and ID
     * Example: PAY-20251208-00001
     */
    public String generateCode(String prefix, Long id) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String paddedId = String.format("%05d", id);
        return String.format("%s-%s-%s", prefix, timestamp, paddedId);
    }

    /**
     * Generate payment code
     */
    public String generatePaymentCode(Long paymentId) {
        return generateCode("PAY", paymentId);
    }

    /**
     * Generate contract code
     */
    public String generateContractCode(Long contractId) {
        return generateCode("CONTRACT", contractId);
    }

    /**
     * Generate invoice code
     */
    public String generateInvoiceCode(Long invoiceId) {
        return generateCode("INV", invoiceId);
    }

    /**
     * Generate partner code
     */
    public String generatePartnerCode(Long partnerId) {
        return generateCode("PARTNER", partnerId);
    }
}
