package kz.nurlybek.coworking_booking_api.dto;

import java.time.LocalTime;

public record LocationResponse(
        Long id,
        String name,
        String address,
        String city,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean active
) {
}
