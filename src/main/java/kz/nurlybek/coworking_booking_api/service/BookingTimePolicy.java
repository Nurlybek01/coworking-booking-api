package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.config.BookingProperties;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Правила допустимого интервала брони: будущее время, длительность, рабочие часы локации. */
@Component
@RequiredArgsConstructor
public class BookingTimePolicy {

    private final BookingProperties properties;
    private final Clock clock;

    public void validate(LocalDateTime start, LocalDateTime end, Location location) {
        if (!end.isAfter(start)) {
            throw new BusinessRuleException("endTime must be after startTime");
        }
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("Booking must start in the future");
        }

        Duration duration = Duration.between(start, end);
        if (duration.compareTo(Duration.ofMinutes(properties.minDurationMinutes())) < 0) {
            throw new BusinessRuleException(
                    "Booking must last at least %d minutes".formatted(properties.minDurationMinutes()));
        }
        if (duration.compareTo(Duration.ofHours(properties.maxDurationHours())) > 0) {
            throw new BusinessRuleException(
                    "Booking must not exceed %d hours".formatted(properties.maxDurationHours()));
        }

        validateWorkingHours(start, end, location);
    }

    private void validateWorkingHours(LocalDateTime start, LocalDateTime end, Location location) {
        LocalDateTime opening = start.toLocalDate().atTime(openingTime(location));
        LocalDateTime closing = closingDateTime(start, location);
        if (start.isBefore(opening) || end.isAfter(closing)) {
            throw new BusinessRuleException("Booking must be within working hours %s-%s"
                    .formatted(opening.toLocalTime(), closing.toLocalTime()));
        }
    }

    private LocalTime openingTime(Location location) {
        return location.getOpeningTime() != null
                ? location.getOpeningTime()
                : LocalTime.of(properties.openHour(), 0);
    }

    // closeHour = 24 означает полночь следующего дня, LocalTime такое значение не представляет.
    private LocalDateTime closingDateTime(LocalDateTime start, Location location) {
        if (location.getClosingTime() != null) {
            return start.toLocalDate().atTime(location.getClosingTime());
        }
        return start.toLocalDate().atStartOfDay().plusHours(properties.closeHour());
    }
}
