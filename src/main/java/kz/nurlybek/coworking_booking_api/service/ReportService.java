package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.security.AuthUser;

import java.time.LocalDate;

public interface ReportService {

    /** Excel-отчёт о загрузке помещений и выручке за период [from, to] включительно. */
    byte[] occupancyReport(AuthUser actor, LocalDate from, LocalDate to);

    /** Word-подтверждение брони (только для подтверждённых или завершённых броней). */
    byte[] bookingConfirmation(AuthUser actor, Long bookingId);
}
