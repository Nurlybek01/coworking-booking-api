package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.WorkplaceRequest;
import kz.nurlybek.coworking_booking_api.dto.WorkplaceResponse;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.Workplace;

public final class WorkplaceMapper {

    private WorkplaceMapper() {
    }

    public static WorkplaceResponse toResponse(Workplace workplace) {
        Location location = workplace.getLocation();
        Long roomId = workplace.getRoom() == null ? null : workplace.getRoom().getId();
        return new WorkplaceResponse(workplace.getId(), workplace.getNumber(), workplace.getType(),
                workplace.getHourlyBasePrice(), workplace.isActive(), location.getId(), location.getName(), roomId);
    }

    public static void updateEntity(Workplace workplace, WorkplaceRequest request, Location location, Room room) {
        workplace.setNumber(request.number());
        workplace.setType(request.type());
        workplace.setHourlyBasePrice(request.hourlyBasePrice());
        workplace.setLocation(location);
        workplace.setRoom(room);
    }
}
