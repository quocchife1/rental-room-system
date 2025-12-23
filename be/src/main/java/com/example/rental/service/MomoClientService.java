package com.example.rental.service;

import com.example.rental.dto.momo.CreateMomoRequest;
import com.example.rental.dto.momo.CreateMomoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class MomoClientService {

    private final WebClient momoWebClient;

    public MomoClientService(WebClient momoWebClient) {
        this.momoWebClient = momoWebClient;
    }

    public CreateMomoResponse createATMPayment(CreateMomoRequest request) {
        System.out.println(">>> CHECK: Hàm createATMPayment ĐÃ ĐƯỢC GỌI!");
        System.out.println(request);
        try {
            return momoWebClient.post()
                    .uri("/create")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CreateMomoResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
        } catch (WebClientResponseException e) {
            throw e; // bubble up with status and body for controller advice
        }
    }

    // Reactive variant if needed elsewhere
    public Mono<CreateMomoResponse> createATMPaymentAsync(CreateMomoRequest request) {
        return momoWebClient.post()
                .uri("/create")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CreateMomoResponse.class)
                .timeout(Duration.ofSeconds(15));
    }
}
