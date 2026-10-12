package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.config.JwtProperties;
import kz.nurlybek.coworking_booking_api.dto.AuthResponse;
import kz.nurlybek.coworking_booking_api.dto.LoginRequest;
import kz.nurlybek.coworking_booking_api.dto.RefreshTokenRequest;
import kz.nurlybek.coworking_booking_api.dto.RegisterRequest;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.InvalidCredentialsException;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String SECRET = "dGVzdC1vbmx5LXNlY3JldC1rZXktZm9yLWp3dC10ZXN0cy0xMjM0NTY3ODkw";

    @Mock private UserRepository userRepository;
    @Mock private AuditLogger auditLogger;

    @SuppressWarnings("deprecation")
    private final PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();
    private final JwtService jwtService = new JwtService(
            new JwtProperties(SECRET, Duration.ofHours(1), Duration.ofDays(7)), Clock.systemUTC());

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, auditLogger);
    }

    @Test
    void registerCreatesClientWithNormalizedEmail() {
        when(userRepository.existsByEmail("new@test.local")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = service.register(
                new RegisterRequest("  New@Test.Local ", "password1", "New User", null));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("new@test.local");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.CLIENT);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.expiresInSeconds()).isEqualTo(3600);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("taken@test.local")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("taken@test.local", "password1", "X", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = TestData.user(1);
        user.setPassword("right-password");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest(user.getEmail(), "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginRejectsDisabledUser() {
        User user = TestData.user(1);
        user.setPassword("right-password");
        user.setEnabled(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest(user.getEmail(), "right-password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshRejectsAccessToken() {
        User user = TestData.user(1);
        user.setPassword("right-password");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        String accessToken = service.login(new LoginRequest(user.getEmail(), "right-password")).accessToken();

        assertThatThrownBy(() -> service.refresh(new RefreshTokenRequest(accessToken)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshIssuesNewTokensForValidRefreshToken() {
        User user = TestData.user(1);
        user.setPassword("right-password");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        String refreshToken = service.login(new LoginRequest(user.getEmail(), "right-password")).refreshToken();

        AuthResponse refreshed = service.refresh(new RefreshTokenRequest(refreshToken));

        assertThat(jwtService.extractEmail(refreshed.accessToken(), JwtService.TokenType.ACCESS))
                .isEqualTo(user.getEmail());
    }
}
