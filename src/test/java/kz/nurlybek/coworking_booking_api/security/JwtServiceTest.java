package kz.nurlybek.coworking_booking_api.security;

import io.jsonwebtoken.JwtException;
import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.config.JwtProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1vbmx5LXNlY3JldC1rZXktZm9yLWp3dC10ZXN0cy0xMjM0NTY3ODkw";
    private static final String OTHER_SECRET = "YW5vdGhlci1zZWNyZXQta2V5LWZvci1qd3QtdGVzdHMtMDk4NzY1NDMyMQ==";
    private static final Instant T0 = Instant.parse("2030-01-07T09:00:00Z");

    private final AuthUser user = TestData.client(1);

    @Test
    void accessTokenRoundTrip() {
        JwtService jwt = jwtAt(T0, SECRET);

        String token = jwt.generateAccessToken(user);

        assertThat(jwt.extractEmail(token, JwtService.TokenType.ACCESS)).isEqualTo(user.email());
    }

    @Test
    void refreshTokenIsNotAcceptedAsAccessToken() {
        JwtService jwt = jwtAt(T0, SECRET);

        String refresh = jwt.generateRefreshToken(user);

        assertThatThrownBy(() -> jwt.extractEmail(refresh, JwtService.TokenType.ACCESS))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        String token = jwtAt(T0, SECRET).generateAccessToken(user);
        JwtService later = jwtAt(T0.plus(Duration.ofHours(2)), SECRET);

        assertThatThrownBy(() -> later.extractEmail(token, JwtService.TokenType.ACCESS))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String foreign = jwtAt(T0, OTHER_SECRET).generateAccessToken(user);

        assertThatThrownBy(() -> jwtAt(T0, SECRET).extractEmail(foreign, JwtService.TokenType.ACCESS))
                .isInstanceOf(JwtException.class);
    }

    private static JwtService jwtAt(Instant now, String secret) {
        return new JwtService(new JwtProperties(secret, Duration.ofHours(1), Duration.ofDays(7)),
                Clock.fixed(now, ZoneOffset.UTC));
    }
}
