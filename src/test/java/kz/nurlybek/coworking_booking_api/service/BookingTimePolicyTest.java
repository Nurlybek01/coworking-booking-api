package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.config.BookingProperties;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.model.Location;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTimePolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 7, 9, 0);
    private static final LocalDateTime TOMORROW_10 = NOW.plusDays(1).withHour(10);

    private final Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    private final BookingTimePolicy policy = new BookingTimePolicy(new BookingProperties(30, 12, 8, 22), clock);
    private final Location location = TestData.location(1);

    @Test
    void acceptsValidInterval() {
        assertThatCode(() -> policy.validate(TOMORROW_10, TOMORROW_10.plusHours(2), location))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsEndNotAfterStart() {
        assertThatThrownBy(() -> policy.validate(TOMORROW_10, TOMORROW_10, location))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("endTime");
    }

    @Test
    void rejectsStartInThePast() {
        LocalDateTime start = NOW.minusHours(1);
        assertThatThrownBy(() -> policy.validate(start, start.plusHours(2), location))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("future");
    }

    @Test
    void rejectsTooShortBooking() {
        assertThatThrownBy(() -> policy.validate(TOMORROW_10, TOMORROW_10.plusMinutes(29), location))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("at least 30 minutes");
    }

    @Test
    void rejectsTooLongBooking() {
        LocalDateTime start = TOMORROW_10.withHour(8);
        assertThatThrownBy(() -> policy.validate(start, start.plusHours(13), location))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("12 hours");
    }

    @Test
    void rejectsBookingOutsideDefaultWorkingHours() {
        LocalDateTime start = TOMORROW_10.withHour(21);
        assertThatThrownBy(() -> policy.validate(start, start.plusHours(2), location))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("working hours");
    }

    @Test
    void usesLocationOwnWorkingHours() {
        location.setOpeningTime(LocalTime.of(11, 0));
        location.setClosingTime(LocalTime.of(18, 0));

        assertThatThrownBy(() -> policy.validate(TOMORROW_10, TOMORROW_10.plusHours(1), location))
                .isInstanceOf(BusinessRuleException.class);
        assertThatCode(() -> policy.validate(TOMORROW_10.withHour(11), TOMORROW_10.withHour(18), location))
                .doesNotThrowAnyException();
    }
}
