package kz.nurlybek.coworking_booking_api.report;

import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Данные отчёта о загрузке и выручке за период [from, to]; не зависят от формата вывода. */
public record OccupancyReport(
        LocalDate from,
        LocalDate to,
        List<ResourceUsage> usage,
        List<LocationRevenue> revenue,
        Map<BookingStatus, Long> statusCounts
) {
    public record ResourceUsage(String location, String resource, long bookings, BigDecimal hours) {
    }

    public record LocationRevenue(String location, BigDecimal amount) {
    }
}
