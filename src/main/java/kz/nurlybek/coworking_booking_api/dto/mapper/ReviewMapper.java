package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.ReviewResponse;
import kz.nurlybek.coworking_booking_api.model.Review;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getRoom().getId(), review.getUser().getId(),
                review.getUser().getFullName(), review.getRating(), review.getComment(), review.getCreatedAt());
    }
}
