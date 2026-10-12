package kz.nurlybek.coworking_booking_api.security;

import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Аутентифицированный пользователь; хранит id, чтобы сервисам не нужно было повторно искать его по email. */
public record AuthUser(Long id, String email, String passwordHash, Role role, boolean enabled)
        implements UserDetails {

    public static AuthUser from(User user) {
        return new AuthUser(user.getId(), user.getEmail(), user.getPassword(), user.getRole(), user.isEnabled());
    }

    /** ADMIN и MANAGER видят и обслуживают чужие брони. */
    public boolean isStaff() {
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    public boolean canAccessDataOf(Long ownerId) {
        return isStaff() || id.equals(ownerId);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "AuthUser[id=%d, email=%s, role=%s]".formatted(id, email, role);
    }
}
