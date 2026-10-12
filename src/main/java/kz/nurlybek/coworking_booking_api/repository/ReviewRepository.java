package kz.nurlybek.coworking_booking_api.repository;

import kz.nurlybek.coworking_booking_api.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    @EntityGraph(attributePaths = "user")
    Page<Review> findByRoomId(Long roomId, Pageable pageable);
}
