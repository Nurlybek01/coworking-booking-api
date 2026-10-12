package kz.nurlybek.coworking_booking_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Явный Clock, чтобы правила, завязанные на «сейчас», можно было тестировать детерминированно. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
