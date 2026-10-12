package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.audit.AuditAction;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.PaymentRequest;
import kz.nurlybek.coworking_booking_api.dto.PaymentResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.PaymentMapper;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Payment;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentStatus;
import kz.nurlybek.coworking_booking_api.repository.PaymentRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Внешнего платёжного шлюза нет: оплата фиксируется сразу как PAID.
 * Повторная оплата одной брони дополнительно защищена unique(booking_id) в БД.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingAccess bookingAccess;
    private final AuditLogger auditLogger;
    private final Clock clock;

    @Override
    @Transactional
    public PaymentResponse pay(AuthUser actor, Long bookingId, PaymentRequest request) {
        Booking booking = bookingAccess.getAccessible(actor, bookingId);
        if (booking.getStatus() != BookingStatus.PENDING || booking.getPayment() != null) {
            throw new ConflictException("Only unpaid pending bookings can be paid");
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .method(request.method())
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now(clock))
                .build();
        booking.setPayment(payment);
        booking.setStatus(BookingStatus.CONFIRMED);
        Payment saved = paymentRepository.save(payment);

        auditLogger.log(AuditAction.PAYMENT_PAID, actor.email(),
                "bookingId=%d amount=%s method=%s".formatted(bookingId, saved.getAmount(), saved.getMethod()));
        return PaymentMapper.toResponse(saved);
    }

    @Override
    public PaymentResponse findByBooking(AuthUser actor, Long bookingId) {
        Booking booking = bookingAccess.getAccessible(actor, bookingId);
        if (booking.getPayment() == null) {
            throw new ResourceNotFoundException("Payment for booking", bookingId);
        }
        return PaymentMapper.toResponse(booking.getPayment());
    }

    @Override
    @Transactional
    public void refundIfPaid(AuthUser actor, Booking booking) {
        Payment payment = booking.getPayment();
        if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
            return;
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        auditLogger.log(AuditAction.PAYMENT_REFUNDED, actor.email(),
                "bookingId=%d amount=%s".formatted(booking.getId(), payment.getAmount()));
    }
}
