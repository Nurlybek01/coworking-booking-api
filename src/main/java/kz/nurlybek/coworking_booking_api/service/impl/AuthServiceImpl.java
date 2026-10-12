package kz.nurlybek.coworking_booking_api.service.impl;

import io.jsonwebtoken.JwtException;
import kz.nurlybek.coworking_booking_api.audit.AuditAction;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.AuthResponse;
import kz.nurlybek.coworking_booking_api.dto.LoginRequest;
import kz.nurlybek.coworking_booking_api.dto.RefreshTokenRequest;
import kz.nurlybek.coworking_booking_api.dto.RegisterRequest;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.InvalidCredentialsException;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.security.JwtService;
import kz.nurlybek.coworking_booking_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogger auditLogger;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .phone(request.phone())
                .role(Role.CLIENT)
                .build());

        auditLogger.log(AuditAction.USER_REGISTERED, email, "userId=" + user.getId());
        return issueTokens(AuthUser.from(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(User::isEnabled)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);

        auditLogger.log(AuditAction.USER_LOGGED_IN, user.getEmail(), "userId=" + user.getId());
        return issueTokens(AuthUser.from(user));
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String email;
        try {
            email = jwtService.extractEmail(request.refreshToken(), JwtService.TokenType.REFRESH);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidCredentialsException();
        }
        User user = userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .orElseThrow(InvalidCredentialsException::new);
        return issueTokens(AuthUser.from(user));
    }

    private AuthResponse issueTokens(AuthUser user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                TOKEN_TYPE,
                jwtService.accessTokenTtl().toSeconds());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
