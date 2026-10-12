package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.BookingFilter;
import kz.nurlybek.coworking_booking_api.dto.BookingRequest;
import kz.nurlybek.coworking_booking_api.dto.BookingResponse;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    BookingResponse create(AuthUser actor, BookingRequest request);

    BookingResponse findById(AuthUser actor, Long id);

    /** Клиент видит только свои брони независимо от userId в фильтре. */
    Page<BookingResponse> search(AuthUser actor, BookingFilter filter, Pageable pageable);

    BookingResponse cancel(AuthUser actor, Long id);

    BookingResponse complete(AuthUser actor, Long id);
}
