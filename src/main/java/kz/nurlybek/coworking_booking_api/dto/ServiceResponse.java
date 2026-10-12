package kz.nurlybek.coworking_booking_api.dto;

import java.math.BigDecimal;

public record ServiceResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean active
) {
}
