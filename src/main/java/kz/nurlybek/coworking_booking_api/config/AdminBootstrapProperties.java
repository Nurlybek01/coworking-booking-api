package kz.nurlybek.coworking_booking_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Первый администратор системы; создаётся только если задан пароль. */
@ConfigurationProperties("app.bootstrap-admin")
public record AdminBootstrapProperties(
        String email,
        String password,
        String fullName
) {
}
