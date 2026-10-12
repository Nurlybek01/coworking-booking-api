package kz.nurlybek.coworking_booking_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 80) String fullName,
        @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "must be a valid phone number") String phone
) {
}
