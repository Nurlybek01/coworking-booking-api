package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.audit.AuditAction;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.ReviewRequest;
import kz.nurlybek.coworking_booking_api.dto.ReviewResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.ReviewMapper;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Review;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import kz.nurlybek.coworking_booking_api.repository.ReviewRepository;
import kz.nurlybek.coworking_booking_api.repository.RoomRepository;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    @Override
    @Transactional
    public ReviewResponse create(AuthUser actor, Long roomId, ReviewRequest request) {
        Room room = getRoom(roomId);
        if (reviewRepository.existsByUserIdAndRoomId(actor.id(), roomId)) {
            throw new ConflictException("You have already reviewed this room");
        }
        if (!bookingRepository.existsByUserIdAndRoomIdAndStatus(actor.id(), roomId, BookingStatus.COMPLETED)) {
            throw new BusinessRuleException("You can review only rooms you have used");
        }

        Review review = reviewRepository.save(Review.builder()
                .user(userRepository.getReferenceById(actor.id()))
                .room(room)
                .rating(request.rating())
                .comment(request.comment())
                .build());

        auditLogger.log(AuditAction.REVIEW_CREATED, actor.email(),
                "roomId=%d rating=%d".formatted(roomId, review.getRating()));
        return ReviewMapper.toResponse(review);
    }

    @Override
    public Page<ReviewResponse> findByRoom(Long roomId, Pageable pageable) {
        getRoom(roomId);
        return reviewRepository.findByRoomId(roomId, pageable).map(ReviewMapper::toResponse);
    }

    private Room getRoom(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }
}
