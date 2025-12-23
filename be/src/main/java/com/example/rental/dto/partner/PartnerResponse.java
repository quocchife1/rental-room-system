package com.example.rental.dto.partner;

import com.example.rental.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerResponse {
    private Long id;
    private String partnerCode;
    private String username;
    private String companyName;
    private String taxCode;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private UserStatus status;
}