package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.UserResponse;
import kz.nurlybek.coworking_booking_api.dto.UserUpdateRequest;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse findById(Long id);

    Page<UserResponse> findAll(Role role, Pageable pageable);

    UserResponse update(AuthUser actor, Long id, UserUpdateRequest request);
}
