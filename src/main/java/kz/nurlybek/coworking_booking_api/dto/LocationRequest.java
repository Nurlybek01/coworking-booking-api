package kz.nurlybek.coworking_booking_api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record LocationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 200) String address,
        @NotBlank @Size(max = 80) String city,
        LocalTime openingTime,
        LocalTime closingTime
) {
    @AssertTrue(message = "openingTime and closingTime must be set together, opening before closing")
    boolean isWorkingHoursValid() {
        if (openingTime == null || closingTime == null) {
            return openingTime == null && closingTime == null;
        }
        return openingTime.isBefore(closingTime);
    }
}
