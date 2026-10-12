package kz.nurlybek.coworking_booking_api.dto;

import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;

import java.time.LocalDateTime;

/** Фильтр списка броней; все поля необязательны. Период [from, to) применяется к startTime. */
public record BookingFilter(
        Long userId,
        Long roomId,
        Long workplaceId,
        Long locationId,
        BookingStatus status,
        LocalDateTime from,
        LocalDateTime to
) {
    public BookingFilter withUserId(Long newUserId) {
        return new BookingFilter(newUserId, roomId, workplaceId, locationId, status, from, to);
    }
}
