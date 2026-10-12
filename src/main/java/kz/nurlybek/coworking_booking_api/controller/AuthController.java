package kz.nurlybek.coworking_booking_api.controller;

import jakarta.validation.Valid;
import kz.nurlybek.coworking_booking_api.dto.AuthResponse;
import kz.nurlybek.coworking_booking_api.dto.LoginRequest;
import kz.nurlybek.coworking_booking_api.dto.RefreshTokenRequest;
import kz.nurlybek.coworking_booking_api.dto.RegisterRequest;
import kz.nurlybek.coworking_booking_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }
}
