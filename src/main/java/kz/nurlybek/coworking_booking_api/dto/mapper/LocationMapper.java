package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.LocationRequest;
import kz.nurlybek.coworking_booking_api.dto.LocationResponse;
import kz.nurlybek.coworking_booking_api.model.Location;

public final class LocationMapper {

    private LocationMapper() {
    }

    public static LocationResponse toResponse(Location location) {
        return new LocationResponse(location.getId(), location.getName(), location.getAddress(),
                location.getCity(), location.getOpeningTime(), location.getClosingTime(), location.isActive());
    }

    public static void updateEntity(Location location, LocationRequest request) {
        location.setName(request.name());
        location.setAddress(request.address());
        location.setCity(request.city());
        location.setOpeningTime(request.openingTime());
        location.setClosingTime(request.closingTime());
    }
}
