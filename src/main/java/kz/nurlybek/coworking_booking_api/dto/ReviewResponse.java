package kz.nurlybek.coworking_booking_api.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long roomId,
        Long userId,
        String userFullName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
