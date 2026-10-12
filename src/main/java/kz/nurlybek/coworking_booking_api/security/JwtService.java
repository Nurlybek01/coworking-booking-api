package kz.nurlybek.coworking_booking_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kz.nurlybek.coworking_booking_api.config.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String TYPE_CLAIM = "type";
    private static final String ROLE_CLAIM = "role";

    public enum TokenType { ACCESS, REFRESH }

    private final SecretKey key;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtService(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.properties = properties;
        this.clock = clock;
    }

    public String generateAccessToken(AuthUser user) {
        return generate(user, TokenType.ACCESS, properties.accessTokenTtl());
    }

    public String generateRefreshToken(AuthUser user) {
        return generate(user, TokenType.REFRESH, properties.refreshTokenTtl());
    }

    public Duration accessTokenTtl() {
        return properties.accessTokenTtl();
    }

    /**
     * Возвращает email владельца токена.
     * Тип проверяется явно, чтобы долгоживущий refresh-токен нельзя было использовать как access.
     *
     * @throws JwtException если токен невалиден, просрочен или другого типа
     */
    public String extractEmail(String token, TokenType expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!expectedType.name().equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new JwtException("Unexpected token type");
        }
        return claims.getSubject();
    }

    private String generate(AuthUser user, TokenType type, Duration ttl) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(user.email())
                .claim(TYPE_CLAIM, type.name())
                .claim(ROLE_CLAIM, user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }
}
