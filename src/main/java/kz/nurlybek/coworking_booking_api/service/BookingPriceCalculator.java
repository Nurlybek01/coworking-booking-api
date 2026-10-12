package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.model.Service;
import kz.nurlybek.coworking_booking_api.model.Tariff;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Стоимость брони = почасовая ставка × длительность + фиксированная цена каждой доп. услуги.
 * Ставка берётся из тарифа, если он указан, иначе — базовая цена ресурса.
 * Длительность учитывается поминутно, итог округляется до копеек (HALF_UP).
 */
@Component
public class BookingPriceCalculator {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    public BigDecimal calculate(BigDecimal baseHourlyRate,
                                Tariff tariff,
                                LocalDateTime start,
                                LocalDateTime end,
                                Collection<Service> services) {
        BigDecimal hourlyRate = tariff == null ? baseHourlyRate : applicableRate(tariff, start.toLocalDate());
        BigDecimal minutes = BigDecimal.valueOf(Duration.between(start, end).toMinutes());

        BigDecimal timeCost = hourlyRate.multiply(minutes).divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
        BigDecimal servicesCost = services.stream()
                .map(Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return timeCost.add(servicesCost).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applicableRate(Tariff tariff, LocalDate date) {
        if (!isApplicable(tariff, date)) {
            throw new BusinessRuleException("Tariff '%s' is not applicable on %s".formatted(tariff.getName(), date));
        }
        return tariff.getPricePerHour();
    }

    static boolean isApplicable(Tariff tariff, LocalDate date) {
        return tariff.isActive()
                && (tariff.getDayOfWeek() == null || tariff.getDayOfWeek() == date.getDayOfWeek())
                && (tariff.getValidFrom() == null || !date.isBefore(tariff.getValidFrom()))
                && (tariff.getValidTo() == null || !date.isAfter(tariff.getValidTo()));
    }
}
