package kz.nurlybek.coworking_booking_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;

import java.math.BigDecimal;

public record WorkplaceRequest(
        @NotBlank @Size(max = 20) String number,
        @NotNull WorkplaceType type,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal hourlyBasePrice,
        @NotNull Long locationId,
        Long roomId
) {
}
