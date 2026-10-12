package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.PaymentRequest;
import kz.nurlybek.coworking_booking_api.dto.PaymentResponse;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Payment;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentMethod;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentStatus;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import kz.nurlybek.coworking_booking_api.repository.PaymentRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 7, 9, 0);

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AuditLogger auditLogger;

    private PaymentServiceImpl service;
    private final AuthUser client = TestData.client(1);
    private Booking booking;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        service = new PaymentServiceImpl(paymentRepository, new BookingAccess(bookingRepository), auditLogger, clock);
        var room = TestData.room(10, TestData.location(1), "1000");
        booking = TestData.booking(7, TestData.user(1), room, BookingStatus.PENDING,
                NOW.plusDays(1), NOW.plusDays(1).plusHours(1));
    }

    @Test
    void payingPendingBookingConfirmsItForFullPrice() {
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = service.pay(client, 7L, new PaymentRequest(PaymentMethod.CARD));

        assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.amount()).isEqualByComparingTo(booking.getTotalPrice());
        assertThat(response.paidAt()).isEqualTo(NOW);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cannotPayBookingTwice() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.pay(client, 7L, new PaymentRequest(PaymentMethod.CASH)))
                .isInstanceOf(ConflictException.class);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void refundMarksPaidPaymentAsRefunded() {
        Payment payment = Payment.builder().booking(booking).amount(BigDecimal.TEN)
                .method(PaymentMethod.CARD).status(PaymentStatus.PAID).build();
        booking.setPayment(payment);

        service.refundIfPaid(client, booking);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void refundWithoutPaymentDoesNothing() {
        service.refundIfPaid(client, booking);

        verifyNoInteractions(auditLogger);
    }

    @Test
    void findByBookingFailsWhenBookingIsUnpaid() {
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.findByBooking(client, 7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
