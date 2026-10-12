package kz.nurlybek.coworking_booking_api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

/** Бронь ровно одного ресурса: переговорной (roomId) или рабочего места (workplaceId). */
public record BookingRequest(
        Long roomId,
        Long workplaceId,
        Long tariffId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Size(max = 20) Set<Long> serviceIds,
        @Size(max = 1000) String notes
) {
    @AssertTrue(message = "exactly one of roomId or workplaceId must be set")
    boolean isSingleResource() {
        return (roomId == null) != (workplaceId == null);
    }

    public Set<Long> serviceIdsOrEmpty() {
        return serviceIds == null ? Set.of() : serviceIds;
    }
}
