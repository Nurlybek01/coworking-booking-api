package kz.nurlybek.coworking_booking_api.dto;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        String name,
        Integer capacity,
        BigDecimal hourlyBasePrice,
        String description,
        boolean active,
        Long locationId,
        String locationName
) {
}
