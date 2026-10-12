package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.ReviewRequest;
import kz.nurlybek.coworking_booking_api.dto.ReviewResponse;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /** Отзыв возможен только после завершённой брони этой комнаты и только один на пользователя. */
    ReviewResponse create(AuthUser actor, Long roomId, ReviewRequest request);

    Page<ReviewResponse> findByRoom(Long roomId, Pageable pageable);
}
