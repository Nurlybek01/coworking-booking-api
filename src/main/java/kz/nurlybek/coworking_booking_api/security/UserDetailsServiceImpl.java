package kz.nurlybek.coworking_booking_api.security;

import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthUser loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(AuthUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
