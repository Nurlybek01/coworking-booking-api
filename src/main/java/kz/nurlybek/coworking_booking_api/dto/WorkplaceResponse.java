package kz.nurlybek.coworking_booking_api.dto;

import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;

import java.math.BigDecimal;

public record WorkplaceResponse(
        Long id,
        String number,
        WorkplaceType type,
        BigDecimal hourlyBasePrice,
        boolean active,
        Long locationId,
        String locationName,
        Long roomId
) {
}
