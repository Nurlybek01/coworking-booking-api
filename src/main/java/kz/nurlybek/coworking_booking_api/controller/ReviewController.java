package kz.nurlybek.coworking_booking_api.controller;

import jakarta.validation.Valid;
import kz.nurlybek.coworking_booking_api.dto.PageResponse;
import kz.nurlybek.coworking_booking_api.dto.ReviewRequest;
import kz.nurlybek.coworking_booking_api.dto.ReviewResponse;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms/{roomId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal AuthUser actor,
                                 @PathVariable Long roomId,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(actor, roomId, request);
    }

    @GetMapping
    public PageResponse<ReviewResponse> findByRoom(
            @PathVariable Long roomId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(reviewService.findByRoom(roomId, pageable));
    }
}
