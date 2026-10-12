package kz.nurlybek.coworking_booking_api.controller;

import kz.nurlybek.coworking_booking_api.dto.PageResponse;
import kz.nurlybek.coworking_booking_api.dto.UserResponse;
import kz.nurlybek.coworking_booking_api.dto.UserUpdateRequest;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser actor) {
        return userService.findById(actor.id());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> findAll(@RequestParam(required = false) Role role,
                                              @PageableDefault(sort = "id", direction = Sort.Direction.ASC)
                                              Pageable pageable) {
        return PageResponse.from(userService.findAll(role, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(@AuthenticationPrincipal AuthUser actor,
                               @PathVariable Long id,
                               @RequestBody UserUpdateRequest request) {
        return userService.update(actor, id, request);
    }
}
