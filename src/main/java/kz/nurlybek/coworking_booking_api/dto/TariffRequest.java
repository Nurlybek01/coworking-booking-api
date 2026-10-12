package kz.nurlybek.coworking_booking_api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

public record TariffRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal pricePerHour,
        DayOfWeek dayOfWeek,
        LocalDate validFrom,
        LocalDate validTo
) {
    @AssertTrue(message = "validFrom must not be after validTo")
    boolean isValidityPeriodValid() {
        return validFrom == null || validTo == null || !validFrom.isAfter(validTo);
    }
}
