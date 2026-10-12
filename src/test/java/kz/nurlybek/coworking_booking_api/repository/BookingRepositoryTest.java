package kz.nurlybek.coworking_booking_api.repository;

import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Проверка JPQL-запросов пересечения интервалов на реальной (in-memory) БД. */
@DataJpaTest
class BookingRepositoryTest {

    private static final LocalDateTime T10 = LocalDateTime.of(2030, 1, 7, 10, 0);

    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private UserRepository userRepository;

    private User user;
    private Room room;

    @BeforeEach
    void setUp() {
        Location location = locationRepository.save(Location.builder()
                .name("Center").address("Abay 1").city("Almaty").build());
        room = roomRepository.save(Room.builder().name("Blue").capacity(4)
                .hourlyBasePrice(new BigDecimal("1000")).location(location).build());
        user = userRepository.save(User.builder().email("u@test.local").password("password-hash")
                .fullName("User").role(Role.CLIENT).build());
        save(T10, T10.plusHours(2), BookingStatus.CONFIRMED);
    }

    @Test
    void detectsOverlappingInterval() {
        assertThat(bookingRepository.existsOverlapForRoom(room.getId(), T10.plusHours(1), T10.plusHours(3)))
                .isTrue();
    }

    @Test
    void adjacentIntervalsDoNotOverlap() {
        assertThat(bookingRepository.existsOverlapForRoom(room.getId(), T10.plusHours(2), T10.plusHours(3)))
                .isFalse();
        assertThat(bookingRepository.existsOverlapForRoom(room.getId(), T10.minusHours(1), T10))
                .isFalse();
    }

    @Test
    void cancelledBookingsDoNotBlockTheSlot() {
        LocalDateTime later = T10.plusHours(5);
        save(later, later.plusHours(1), BookingStatus.CANCELLED);

        assertThat(bookingRepository.existsOverlapForRoom(room.getId(), later, later.plusHours(1))).isFalse();
    }

    @Test
    void availabilitySearchExcludesBusyRooms() {
        assertThat(roomRepository.findAvailable(T10, T10.plusHours(1), null, null, Pageable.unpaged()))
                .isEmpty();
        assertThat(roomRepository.findAvailable(T10.plusHours(2), T10.plusHours(3), null, 2, Pageable.unpaged()))
                .extracting(Room::getName).containsExactly("Blue");
    }

    private void save(LocalDateTime start, LocalDateTime end, BookingStatus status) {
        bookingRepository.save(Booking.builder().user(user).room(room)
                .startTime(start).endTime(end).status(status).totalPrice(BigDecimal.TEN).build());
    }
}
