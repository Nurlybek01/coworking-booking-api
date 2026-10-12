package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.PaymentRequest;
import kz.nurlybek.coworking_booking_api.dto.PaymentResponse;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.security.AuthUser;

public interface PaymentService {

    /** Оплачивает ожидающую бронь на её полную стоимость и подтверждает её. */
    PaymentResponse pay(AuthUser actor, Long bookingId, PaymentRequest request);

    PaymentResponse findByBooking(AuthUser actor, Long bookingId);

    /** Возвращает деньги за бронь, если она была оплачена; иначе ничего не делает. */
    void refundIfPaid(AuthUser actor, Booking booking);
}
