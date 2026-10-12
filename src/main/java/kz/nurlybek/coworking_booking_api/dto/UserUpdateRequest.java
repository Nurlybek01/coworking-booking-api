package kz.nurlybek.coworking_booking_api.dto;

import kz.nurlybek.coworking_booking_api.model.enums.Role;

/** Изменение пользователя администратором; null-поля не меняются. */
public record UserUpdateRequest(
        Role role,
        Boolean enabled
) {
}
