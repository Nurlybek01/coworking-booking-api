package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.RoomRequest;
import kz.nurlybek.coworking_booking_api.dto.RoomResponse;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomResponse toResponse(Room room) {
        Location location = room.getLocation();
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getHourlyBasePrice(),
                room.getDescription(), room.isActive(), location.getId(), location.getName());
    }

    public static void updateEntity(Room room, RoomRequest request, Location location) {
        room.setName(request.name());
        room.setCapacity(request.capacity());
        room.setHourlyBasePrice(request.hourlyBasePrice());
        room.setDescription(request.description());
        room.setLocation(location);
    }
}
