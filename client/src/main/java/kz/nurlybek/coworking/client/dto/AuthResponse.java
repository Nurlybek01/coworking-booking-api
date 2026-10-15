package kz.nurlybek.coworking.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    public String accessToken;
    public String refreshToken;
    public String tokenType;
    public long expiresInSeconds;

    public AuthResponse() {}
}