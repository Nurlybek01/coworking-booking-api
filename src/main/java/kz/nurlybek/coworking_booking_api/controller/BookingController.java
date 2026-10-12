package kz.nurlybek.coworking_booking_api.controller;

import jakarta.validation.Valid;
import kz.nurlybek.coworking_booking_api.dto.BookingFilter;
import kz.nurlybek.coworking_booking_api.dto.BookingRequest;
import kz.nurlybek.coworking_booking_api.dto.BookingResponse;
import kz.nurlybek.coworking_booking_api.dto.PageResponse;
import kz.nurlybek.coworking_booking_api.dto.PaymentRequest;
import kz.nurlybek.coworking_booking_api.dto.PaymentResponse;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.BookingService;
import kz.nurlybek.coworking_booking_api.service.PaymentService;
import kz.nurlybek.coworking_booking_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@AuthenticationPrincipal AuthUser actor,
                                  @Valid @RequestBody BookingRequest request) {
        return bookingService.create(actor, request);
    }

    /** Клиент получает только свои брони; персонал может фильтровать по любому пользователю. */
    @GetMapping
    public PageResponse<BookingResponse> search(
            @AuthenticationPrincipal AuthUser actor,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long workplaceId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {
        var filter = new BookingFilter(userId, roomId, workplaceId, locationId, status, from, to);
        return PageResponse.from(bookingService.search(actor, filter, pageable));
    }

    @GetMapping("/{id}")
    public BookingResponse findById(@AuthenticationPrincipal AuthUser actor, @PathVariable Long id) {
        return bookingService.findById(actor, id);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal AuthUser actor, @PathVariable Long id) {
        return bookingService.cancel(actor, id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public BookingResponse complete(@AuthenticationPrincipal AuthUser actor, @PathVariable Long id) {
        return bookingService.complete(actor, id);
    }

    @PostMapping("/{id}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@AuthenticationPrincipal AuthUser actor,
                               @PathVariable Long id,
                               @Valid @RequestBody PaymentRequest request) {
        return paymentService.pay(actor, id, request);
    }

    @GetMapping("/{id}/payment")
    public PaymentResponse findPayment(@AuthenticationPrincipal AuthUser actor, @PathVariable Long id) {
        return paymentService.findByBooking(actor, id);
    }

    @GetMapping("/{id}/confirmation")
    public ResponseEntity<byte[]> confirmation(@AuthenticationPrincipal AuthUser actor, @PathVariable Long id) {
        byte[] document = reportService.bookingConfirmation(actor, id);
        return Downloads.attachment(document, "booking-%d-confirmation.docx".formatted(id), Downloads.DOCX);
    }
}
