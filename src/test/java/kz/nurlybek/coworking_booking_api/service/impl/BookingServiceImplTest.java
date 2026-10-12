package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.config.BookingProperties;
import kz.nurlybek.coworking_booking_api.dto.BookingRequest;
import kz.nurlybek.coworking_booking_api.dto.BookingResponse;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import kz.nurlybek.coworking_booking_api.repository.RoomRepository;
import kz.nurlybek.coworking_booking_api.repository.ServiceRepository;
import kz.nurlybek.coworking_booking_api.repository.TariffRepository;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.repository.WorkplaceRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.BookingPriceCalculator;
import kz.nurlybek.coworking_booking_api.service.BookingTimePolicy;
import kz.nurlybek.coworking_booking_api.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 7, 9, 0);
    private static final LocalDateTime START = NOW.plusDays(1).withHour(10);
    private static final LocalDateTime END = START.plusHours(2);

    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private WorkplaceRepository workplaceRepository;
    @Mock private TariffRepository tariffRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private PaymentService paymentService;
    @Mock private AuditLogger auditLogger;

    private BookingServiceImpl service;

    private final AuthUser client = TestData.client(1);
    private final User clientUser = TestData.user(1);
    private final Location location = TestData.location(1);
    private final Room room = TestData.room(10, location, "1000");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        service = new BookingServiceImpl(bookingRepository, userRepository, roomRepository, workplaceRepository,
                tariffRepository, serviceRepository,
                new BookingTimePolicy(new BookingProperties(30, 12, 8, 22), clock),
                new BookingPriceCalculator(), paymentService, new BookingAccess(bookingRepository),
                auditLogger, clock);
    }

    @Test
    void createsPendingRoomBookingWithCalculatedPrice() {
        when(userRepository.getReferenceById(1L)).thenReturn(clientUser);
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(serviceRepository.findByIdInAndActiveTrue(Set.of(5L))).thenReturn(List.of(TestData.service(5, "300")));
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking booking = inv.getArgument(0);
            booking.setId(42L);
            return booking;
        });

        BookingResponse response = service.create(client, roomRequest(Set.of(5L)));

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.totalPrice()).isEqualByComparingTo("2300.00");
        assertThat(response.roomId()).isEqualTo(10L);
        assertThat(response.locationName()).isEqualTo(location.getName());
    }

    @Test
    void rejectsOverlappingRoomBooking() {
        when(userRepository.getReferenceById(1L)).thenReturn(clientUser);
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlapForRoom(10L, START, END)).thenReturn(true);

        assertThatThrownBy(() -> service.create(client, roomRequest(null)))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void inactiveRoomIsNotBookable() {
        room.setActive(false);
        when(userRepository.getReferenceById(1L)).thenReturn(clientUser);
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.create(client, roomRequest(null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsUnknownOrInactiveServices() {
        when(userRepository.getReferenceById(1L)).thenReturn(clientUser);
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(serviceRepository.findByIdInAndActiveTrue(Set.of(5L, 6L))).thenReturn(List.of(TestData.service(5, "1")));

        assertThatThrownBy(() -> service.create(client, roomRequest(Set.of(5L, 6L))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("6");
    }

    @Test
    void cancelRefundsPaymentAndMarksCancelled() {
        Booking booking = TestData.booking(7, clientUser, room, BookingStatus.CONFIRMED, START, END);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        BookingResponse response = service.cancel(client, 7L);

        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        verify(paymentService).refundIfPaid(client, booking);
    }

    @Test
    void completedBookingCannotBeCancelled() {
        Booking booking = TestData.booking(7, clientUser, room, BookingStatus.COMPLETED, START, END);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.cancel(client, 7L)).isInstanceOf(ConflictException.class);
        verify(paymentService, never()).refundIfPaid(any(), any());
    }

    @Test
    void clientCannotCancelStartedBookingButStaffCan() {
        Booking booking = TestData.booking(7, clientUser, room, BookingStatus.CONFIRMED,
                NOW.minusHours(1), NOW.plusHours(1));
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.cancel(client, 7L)).isInstanceOf(BusinessRuleException.class);
        assertThat(service.cancel(TestData.manager(2), 7L).status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void clientCannotSeeForeignBooking() {
        Booking booking = TestData.booking(7, TestData.user(99), room, BookingStatus.PENDING, START, END);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.findById(client, 7L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void onlyConfirmedBookingCanBeCompleted() {
        Booking booking = TestData.booking(7, clientUser, room, BookingStatus.PENDING, START, END);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.complete(TestData.manager(2), 7L)).isInstanceOf(ConflictException.class);

        booking.setStatus(BookingStatus.CONFIRMED);
        assertThat(service.complete(TestData.manager(2), 7L).status()).isEqualTo(BookingStatus.COMPLETED);
    }

    private static BookingRequest roomRequest(Set<Long> serviceIds) {
        return new BookingRequest(10L, null, null, START, END, serviceIds, null);
    }
}
