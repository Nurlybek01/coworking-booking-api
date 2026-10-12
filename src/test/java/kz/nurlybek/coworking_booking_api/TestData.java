package kz.nurlybek.coworking_booking_api;

import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.Service;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.Workplace;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;
import kz.nurlybek.coworking_booking_api.security.AuthUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Фабрика тестовых сущностей с разумными значениями по умолчанию. */
public final class TestData {

    private TestData() {
    }

    public static AuthUser client(long id) {
        return new AuthUser(id, "client" + id + "@test.local", "hash", Role.CLIENT, true);
    }

    public static AuthUser manager(long id) {
        return new AuthUser(id, "manager" + id + "@test.local", "hash", Role.MANAGER, true);
    }

    public static User user(long id) {
        return User.builder().id(id).email("user" + id + "@test.local").password("hash")
                .fullName("User " + id).role(Role.CLIENT).build();
    }

    public static Location location(long id) {
        return Location.builder().id(id).name("Location " + id).address("Street " + id).city("Almaty").build();
    }

    public static Room room(long id, Location location, String hourlyPrice) {
        return Room.builder().id(id).name("Room " + id).capacity(6)
                .hourlyBasePrice(new BigDecimal(hourlyPrice)).location(location).build();
    }

    public static Workplace workplace(long id, Location location, String hourlyPrice) {
        return Workplace.builder().id(id).number("W-" + id).type(WorkplaceType.HOT_DESK)
                .hourlyBasePrice(new BigDecimal(hourlyPrice)).location(location).build();
    }

    public static Service service(long id, String price) {
        return Service.builder().id(id).name("Service " + id).price(new BigDecimal(price)).build();
    }

    public static Booking booking(long id, User user, Room room, BookingStatus status,
                                  LocalDateTime start, LocalDateTime end) {
        return Booking.builder().id(id).user(user).room(room).status(status)
                .startTime(start).endTime(end).totalPrice(new BigDecimal("100.00")).build();
    }
}
