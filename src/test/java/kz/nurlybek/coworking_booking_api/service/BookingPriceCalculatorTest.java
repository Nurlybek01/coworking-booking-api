package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.model.Tariff;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingPriceCalculatorTest {

    // 2030-01-07 — понедельник.
    private static final LocalDateTime MONDAY_10 = LocalDateTime.of(2030, 1, 7, 10, 0);

    private final BookingPriceCalculator calculator = new BookingPriceCalculator();

    @Test
    void usesBaseRateWhenNoTariff() {
        BigDecimal price = calculator.calculate(new BigDecimal("1000"), null,
                MONDAY_10, MONDAY_10.plusHours(2), List.of());

        assertThat(price).isEqualByComparingTo("2000.00");
    }

    @Test
    void chargesPartialHoursPerMinuteWithRounding() {
        // 333.33/ч × 50 мин = 277.775 → 277.78
        BigDecimal price = calculator.calculate(new BigDecimal("333.33"), null,
                MONDAY_10, MONDAY_10.plusMinutes(50), List.of());

        assertThat(price).isEqualByComparingTo("277.78");
        assertThat(price.scale()).isEqualTo(2);
    }

    @Test
    void addsFixedPriceOfEachService() {
        BigDecimal price = calculator.calculate(new BigDecimal("1000"), null,
                MONDAY_10, MONDAY_10.plusHours(1),
                List.of(TestData.service(1, "500"), TestData.service(2, "250.50")));

        assertThat(price).isEqualByComparingTo("1750.50");
    }

    @Test
    void tariffRateOverridesBaseRate() {
        Tariff tariff = Tariff.builder().name("Weekday").pricePerHour(new BigDecimal("800"))
                .dayOfWeek(DayOfWeek.MONDAY).build();

        BigDecimal price = calculator.calculate(new BigDecimal("1000"), tariff,
                MONDAY_10, MONDAY_10.plusHours(3), List.of());

        assertThat(price).isEqualByComparingTo("2400.00");
    }

    @Test
    void rejectsTariffForAnotherDayOfWeek() {
        Tariff tariff = Tariff.builder().name("Weekend").pricePerHour(new BigDecimal("500"))
                .dayOfWeek(DayOfWeek.SATURDAY).build();

        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("1000"), tariff,
                MONDAY_10, MONDAY_10.plusHours(1), List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Weekend");
    }

    @Test
    void rejectsInactiveTariff() {
        Tariff tariff = Tariff.builder().name("Old").pricePerHour(new BigDecimal("500")).active(false).build();

        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("1000"), tariff,
                MONDAY_10, MONDAY_10.plusHours(1), List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void tariffValidityPeriodIsInclusive() {
        LocalDate day = MONDAY_10.toLocalDate();
        Tariff tariff = Tariff.builder().name("Promo").pricePerHour(BigDecimal.ONE)
                .validFrom(day).validTo(day).build();

        assertThat(BookingPriceCalculator.isApplicable(tariff, day)).isTrue();
        assertThat(BookingPriceCalculator.isApplicable(tariff, day.plusDays(1))).isFalse();
        assertThat(BookingPriceCalculator.isApplicable(tariff, day.minusDays(1))).isFalse();
    }
}
