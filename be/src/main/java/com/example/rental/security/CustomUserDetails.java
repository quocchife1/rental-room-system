package com.example.rental.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final String userRole;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String userRole) {
        super(username, password, authorities);
        this.userRole = userRole;
    }
}
