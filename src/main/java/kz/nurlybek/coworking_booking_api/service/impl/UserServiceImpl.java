package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.audit.AuditAction;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.UserResponse;
import kz.nurlybek.coworking_booking_api.dto.UserUpdateRequest;
import kz.nurlybek.coworking_booking_api.dto.mapper.UserMapper;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    @Override
    public UserResponse findById(Long id) {
        return UserMapper.toResponse(getUser(id));
    }

    @Override
    public Page<UserResponse> findAll(Role role, Pageable pageable) {
        Page<User> users = role == null ? userRepository.findAll(pageable) : userRepository.findByRole(role, pageable);
        return users.map(UserMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse update(AuthUser actor, Long id, UserUpdateRequest request) {
        // Защита от случайной потери доступа: администратор не может разжаловать или заблокировать сам себя.
        if (actor.id().equals(id)) {
            throw new BusinessRuleException("You cannot change your own role or status");
        }
        User user = getUser(id);
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        auditLogger.log(AuditAction.USER_UPDATED, actor.email(),
                "userId=%d role=%s enabled=%s".formatted(id, user.getRole(), user.isEnabled()));
        return UserMapper.toResponse(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
