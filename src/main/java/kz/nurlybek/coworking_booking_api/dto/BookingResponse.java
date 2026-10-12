package kz.nurlybek.coworking_booking_api.dto;

import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long userId,
        String userFullName,
        Long roomId,
        String roomName,
        Long workplaceId,
        String workplaceNumber,
        Long locationId,
        String locationName,
        String locationAddress,
        Long tariffId,
        String tariffName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status,
        BigDecimal totalPrice,
        String notes,
        List<ServiceResponse> services,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt
) {
}
