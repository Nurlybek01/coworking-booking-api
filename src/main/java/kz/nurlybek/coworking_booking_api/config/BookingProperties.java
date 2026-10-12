package kz.nurlybek.coworking_booking_api.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Правила бронирования. openHour/closeHour — рабочие часы по умолчанию
 * для локаций, у которых не задано собственное расписание.
 */
@Validated
@ConfigurationProperties("app.booking")
public record BookingProperties(
        @Min(1) int minDurationMinutes,
        @Min(1) int maxDurationHours,
        @Min(0) @Max(23) int openHour,
        @Min(1) @Max(24) int closeHour
) {
}
