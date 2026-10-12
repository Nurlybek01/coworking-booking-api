package kz.nurlybek.coworking_booking_api.dto;

import jakarta.validation.constraints.NotNull;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentMethod;

public record PaymentRequest(@NotNull PaymentMethod method) {
}
