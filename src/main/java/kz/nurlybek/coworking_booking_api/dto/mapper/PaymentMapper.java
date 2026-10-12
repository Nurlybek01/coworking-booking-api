package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.PaymentResponse;
import kz.nurlybek.coworking_booking_api.model.Payment;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getBooking().getId(), payment.getAmount(),
                payment.getMethod(), payment.getStatus(), payment.getPaidAt());
    }
}
