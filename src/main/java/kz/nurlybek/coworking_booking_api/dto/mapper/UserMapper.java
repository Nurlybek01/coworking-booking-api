package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.UserResponse;
import kz.nurlybek.coworking_booking_api.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}
