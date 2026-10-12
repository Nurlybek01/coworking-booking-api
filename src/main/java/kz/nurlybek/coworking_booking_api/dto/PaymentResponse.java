package kz.nurlybek.coworking_booking_api.dto;

import kz.nurlybek.coworking_booking_api.model.enums.PaymentMethod;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long bookingId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        LocalDateTime paidAt
) {
}
