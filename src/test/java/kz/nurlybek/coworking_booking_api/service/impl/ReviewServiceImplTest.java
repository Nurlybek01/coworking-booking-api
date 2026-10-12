package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.ReviewRequest;
import kz.nurlybek.coworking_booking_api.dto.ReviewResponse;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import kz.nurlybek.coworking_booking_api.repository.ReviewRepository;
import kz.nurlybek.coworking_booking_api.repository.RoomRepository;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogger auditLogger;

    @InjectMocks private ReviewServiceImpl service;

    private final AuthUser client = TestData.client(1);
    private final Room room = TestData.room(10, TestData.location(1), "1000");

    @Test
    void reviewRequiresCompletedBookingOfTheRoom() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByUserIdAndRoomIdAndStatus(1L, 10L, BookingStatus.COMPLETED)).thenReturn(false);

        assertThatThrownBy(() -> service.create(client, 10L, new ReviewRequest(5, "Great")))
                .isInstanceOf(BusinessRuleException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void secondReviewOfSameRoomIsRejected() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(reviewRepository.existsByUserIdAndRoomId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(client, 10L, new ReviewRequest(4, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createsReviewAfterCompletedBooking() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByUserIdAndRoomIdAndStatus(1L, 10L, BookingStatus.COMPLETED)).thenReturn(true);
        when(userRepository.getReferenceById(1L)).thenReturn(TestData.user(1));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = service.create(client, 10L, new ReviewRequest(5, "Great"));

        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.roomId()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(1L);
    }
}
