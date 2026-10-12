package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.audit.AuditAction;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.mapper.BookingMapper;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.model.enums.PaymentStatus;
import kz.nurlybek.coworking_booking_api.report.BookingConfirmationWriter;
import kz.nurlybek.coworking_booking_api.report.ExcelReportWriter;
import kz.nurlybek.coworking_booking_api.report.OccupancyReport;
import kz.nurlybek.coworking_booking_api.report.OccupancyReport.LocationRevenue;
import kz.nurlybek.coworking_booking_api.report.OccupancyReport.ResourceUsage;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    // Отчёт собирается в памяти, поэтому период ограничен, чтобы один запрос не выгрузил всю историю.
    static final int MAX_PERIOD_DAYS = 366;
    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final Set<BookingStatus> CONFIRMABLE = Set.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);

    private final BookingRepository bookingRepository;
    private final BookingAccess bookingAccess;
    private final ExcelReportWriter excelWriter;
    private final BookingConfirmationWriter confirmationWriter;
    private final AuditLogger auditLogger;

    @Override
    public byte[] occupancyReport(AuthUser actor, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessRuleException("'from' must not be after 'to'");
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_PERIOD_DAYS) {
            throw new BusinessRuleException("Report period must not exceed %d days".formatted(MAX_PERIOD_DAYS));
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<Booking> bookings = bookingRepository.findActiveStartingBetween(start, end);
        OccupancyReport report = new OccupancyReport(from, to, usage(bookings), revenue(bookings), statusCounts(start, end));

        auditLogger.log(AuditAction.REPORT_GENERATED, actor.email(), "type=occupancy from=%s to=%s".formatted(from, to));
        return excelWriter.write(report);
    }

    @Override
    public byte[] bookingConfirmation(AuthUser actor, Long bookingId) {
        Booking booking = bookingAccess.getAccessible(actor, bookingId);
        if (!CONFIRMABLE.contains(booking.getStatus())) {
            throw new ConflictException("Confirmation is available only for confirmed or completed bookings");
        }
        return confirmationWriter.write(BookingMapper.toResponse(booking));
    }

    private static List<ResourceUsage> usage(List<Booking> bookings) {
        // Группируем по id ресурса: имена комнат внутри локации не обязаны быть уникальными.
        Map<String, UsageAccumulator> byResource = new HashMap<>();
        for (Booking booking : bookings) {
            String key = booking.getRoom() != null
                    ? "room:" + booking.getRoom().getId()
                    : "workplace:" + booking.getWorkplace().getId();
            byResource.computeIfAbsent(key, k -> new UsageAccumulator(location(booking).getName(), resourceName(booking)))
                    .add(Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes());
        }
        return byResource.values().stream()
                .map(UsageAccumulator::toUsage)
                .sorted(Comparator.comparing(ResourceUsage::location).thenComparing(ResourceUsage::resource))
                .toList();
    }

    private static List<LocationRevenue> revenue(List<Booking> bookings) {
        Map<String, BigDecimal> byLocation = new TreeMap<>();
        for (Booking booking : bookings) {
            if (booking.getPayment() != null && booking.getPayment().getStatus() == PaymentStatus.PAID) {
                byLocation.merge(location(booking).getName(), booking.getPayment().getAmount(), BigDecimal::add);
            }
        }
        List<LocationRevenue> result = new ArrayList<>();
        byLocation.forEach((location, amount) -> result.add(new LocationRevenue(location, amount)));
        return result;
    }

    private Map<BookingStatus, Long> statusCounts(LocalDateTime start, LocalDateTime end) {
        Map<BookingStatus, Long> counts = new EnumMap<>(BookingStatus.class);
        bookingRepository.countByStatusBetween(start, end)
                .forEach(row -> counts.put(row.getStatus(), row.getCount()));
        return counts;
    }

    private static String resourceName(Booking booking) {
        return booking.getRoom() != null
                ? "Комната: " + booking.getRoom().getName()
                : "Место: " + booking.getWorkplace().getNumber();
    }

    private static Location location(Booking booking) {
        return booking.getRoom() != null ? booking.getRoom().getLocation() : booking.getWorkplace().getLocation();
    }

    private static final class UsageAccumulator {
        private final String location;
        private final String resource;
        private long bookings;
        private long minutes;

        private UsageAccumulator(String location, String resource) {
            this.location = location;
            this.resource = resource;
        }

        private void add(long bookingMinutes) {
            bookings++;
            minutes += bookingMinutes;
        }

        private ResourceUsage toUsage() {
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
            return new ResourceUsage(location, resource, bookings, hours);
        }
    }
}
