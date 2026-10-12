package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.AuthResponse;
import kz.nurlybek.coworking_booking_api.dto.LoginRequest;
import kz.nurlybek.coworking_booking_api.dto.RefreshTokenRequest;
import kz.nurlybek.coworking_booking_api.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}
