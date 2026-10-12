package kz.nurlybek.coworking_booking_api.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

public record TariffResponse(
        Long id,
        String name,
        BigDecimal pricePerHour,
        DayOfWeek dayOfWeek,
        LocalDate validFrom,
        LocalDate validTo,
        boolean active
) {
}
