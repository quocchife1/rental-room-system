package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.service.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/momo")
@RequiredArgsConstructor
@Slf4j
public class MomoCallbackController {

    private final MomoService momoService;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @GetMapping("/return")
    public ResponseEntity<ApiResponseDto<java.util.Map<String, String>>> momoReturn(
            @RequestParam String orderId,
            @RequestParam(required = false) String returnUrl
    ) {
        boolean ok = false;
        try {
            ok = momoService.handleMomoReturn(orderId);
        } catch (Exception ex) {
            log.error("MoMo return handling failed", ex);
            ok = false;
        }

        String feBase = (frontendBaseUrl == null || frontendBaseUrl.isBlank()) ? "http://localhost:3000" : frontendBaseUrl;
        String target;
        if (returnUrl == null || returnUrl.isBlank()) {
            target = feBase;
        } else if (returnUrl.startsWith("/")) {
            target = feBase + returnUrl;
        } else if (returnUrl.startsWith(feBase)) {
            target = returnUrl;
        } else {
            // prevent open redirect to untrusted domains
            target = feBase;
        }

        try {
            java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
                    .fromUriString(target)
                    .queryParam("momo", ok ? "success" : "failed")
                    .queryParam("orderId", orderId)
                    .build()
                    .toUri();

            return ResponseEntity.status(302)
                    .location(uri)
                    .body(ApiResponseDto.success(302, "Redirecting", java.util.Collections.singletonMap("location", uri.toString())));
        } catch (Exception e) {
            // Fallback: return JSON only
            return ResponseEntity.ok(ApiResponseDto.success(200, ok ? "OK" : "FAILED", java.util.Collections.singletonMap("orderId", orderId)));
        }
    }

    @PostMapping(path = "/ipn-handler", consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity<ApiResponseDto<String>> handleIpn(@RequestBody(required = false) Map<String, Object> payload,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Map<String, String> form) {
        try {
            Map<String, Object> effective = payload != null ? payload
                    : (form != null ? new java.util.HashMap<>(form) : java.util.Collections.emptyMap());
            log.info("Received MoMo IPN: {}", effective);
            momoService.handleMomoCallback(effective);
            return ResponseEntity.ok(ApiResponseDto.success(200, "IPN processed", null));
        } catch (Exception ex) {
            log.error("MoMo IPN handling failed", ex);
            // During debugging, still return 200 to avoid provider retry storms; adjust per
            // your policy
            return ResponseEntity.ok(ApiResponseDto.success(200, "IPN received (processing error logged)", null));
        }
    }
}
