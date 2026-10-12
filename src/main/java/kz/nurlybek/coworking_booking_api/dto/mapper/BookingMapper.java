package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.BookingResponse;
import kz.nurlybek.coworking_booking_api.dto.ServiceResponse;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.Tariff;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.Workplace;

import java.util.Comparator;
import java.util.List;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingResponse toResponse(Booking booking) {
        User user = booking.getUser();
        Room room = booking.getRoom();
        Workplace workplace = booking.getWorkplace();
        Tariff tariff = booking.getTariff();
        Location location = room != null ? room.getLocation() : workplace.getLocation();

        List<ServiceResponse> services = booking.getServices().stream()
                .map(ServiceMapper::toResponse)
                .sorted(Comparator.comparing(ServiceResponse::id))
                .toList();

        return new BookingResponse(
                booking.getId(),
                user.getId(),
                user.getFullName(),
                room == null ? null : room.getId(),
                room == null ? null : room.getName(),
                workplace == null ? null : workplace.getId(),
                workplace == null ? null : workplace.getNumber(),
                location.getId(),
                location.getName(),
                location.getAddress(),
                tariff == null ? null : tariff.getId(),
                tariff == null ? null : tariff.getName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getNotes(),
                services,
                booking.getPayment() == null ? null : booking.getPayment().getStatus(),
                booking.getCreatedAt());
    }
}
