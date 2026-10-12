package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.UserResponse;
import kz.nurlybek.coworking_booking_api.dto.UserUpdateRequest;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private AuditLogger auditLogger;

    @InjectMocks private UserServiceImpl service;

    private final AuthUser admin = new AuthUser(1L, "admin@test.local", "hash", Role.ADMIN, true);

    @Test
    void adminCannotChangeOwnRoleOrStatus() {
        assertThatThrownBy(() -> service.update(admin, 1L, new UserUpdateRequest(Role.CLIENT, false)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateChangesOnlyProvidedFields() {
        User user = TestData.user(2);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserResponse response = service.update(admin, 2L, new UserUpdateRequest(Role.MANAGER, null));

        assertThat(response.role()).isEqualTo(Role.MANAGER);
        assertThat(response.enabled()).isTrue();
    }
}
