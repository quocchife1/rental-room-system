package com.example.rental.dto.partnerpost;

import com.example.rental.entity.PartnerPost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostResponse {
    private PartnerPost post;
    private String paymentUrl;
}