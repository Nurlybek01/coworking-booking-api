package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.TestData;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Payment;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.User;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentMethod;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentStatus;
import kz.nurlybek.coworking_booking_api.report.BookingConfirmationWriter;
import kz.nurlybek.coworking_booking_api.report.ExcelReportWriter;
import kz.nurlybek.coworking_booking_api.report.OccupancyReport;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    private static final LocalDate DAY = LocalDate.of(2030, 1, 7);

    @Mock private BookingRepository bookingRepository;
    @Mock private ExcelReportWriter excelWriter;
    @Mock private BookingConfirmationWriter confirmationWriter;
    @Mock private AuditLogger auditLogger;

    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(bookingRepository, new BookingAccess(bookingRepository),
                excelWriter, confirmationWriter, auditLogger);
    }

    @Test
    void aggregatesHoursPerResourceAndPaidRevenuePerLocation() {
        Location location = TestData.location(1);
        Room room = TestData.room(10, location, "1000");
        User user = TestData.user(1);
        LocalDateTime start = DAY.atTime(10, 0);
        Booking paid = TestData.booking(1, user, room, BookingStatus.CONFIRMED, start, start.plusHours(2));
        paid.setPayment(payment(paid, "2000.00", PaymentStatus.PAID));
        Booking unpaid = TestData.booking(2, user, room, BookingStatus.PENDING,
                start.plusHours(3), start.plusHours(3).plusMinutes(30));

        when(bookingRepository.findActiveStartingBetween(DAY.atStartOfDay(), DAY.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(paid, unpaid));
        when(bookingRepository.countByStatusBetween(any(), any())).thenReturn(List.of());
        when(excelWriter.write(any())).thenReturn(new byte[]{1});

        service.occupancyReport(TestData.manager(5), DAY, DAY);

        ArgumentCaptor<OccupancyReport> captor = ArgumentCaptor.forClass(OccupancyReport.class);
        verify(excelWriter).write(captor.capture());
        OccupancyReport report = captor.getValue();
        assertThat(report.usage()).singleElement().satisfies(usage -> {
            assertThat(usage.bookings()).isEqualTo(2);
            assertThat(usage.hours()).isEqualByComparingTo("2.50");
        });
        assertThat(report.revenue()).singleElement().satisfies(revenue ->
                assertThat(revenue.amount()).isEqualByComparingTo("2000.00"));
    }

    @Test
    void rejectsInvertedOrTooLongPeriod() {
        assertThatThrownBy(() -> service.occupancyReport(TestData.manager(5), DAY, DAY.minusDays(1)))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.occupancyReport(TestData.manager(5), DAY, DAY.plusDays(400)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void confirmationIsNotAvailableForPendingBooking() {
        Room room = TestData.room(10, TestData.location(1), "1000");
        Booking pending = TestData.booking(3, TestData.user(1), room, BookingStatus.PENDING,
                DAY.atTime(10, 0), DAY.atTime(11, 0));
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.bookingConfirmation(TestData.client(1), 3L))
                .isInstanceOf(ConflictException.class);
    }

    private static Payment payment(Booking booking, String amount, PaymentStatus status) {
        return Payment.builder().booking(booking).amount(new BigDecimal(amount))
                .method(PaymentMethod.CARD).status(status).build();
    }
}
