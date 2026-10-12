package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Единая проверка «бронь существует и актор — её владелец или персонал» для всех сервисов вокруг брони. */
@Component
@RequiredArgsConstructor
class BookingAccess {

    private final BookingRepository bookingRepository;

    Booking getAccessible(AuthUser actor, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (!actor.canAccessDataOf(booking.getUser().getId())) {
            throw new AccessDeniedException("Booking %d belongs to another user".formatted(bookingId));
        }
        return booking;
    }
}
