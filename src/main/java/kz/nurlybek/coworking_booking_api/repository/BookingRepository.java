package kz.nurlybek.coworking_booking_api.repository;

import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // Связи to-one подтягиваются одним запросом, чтобы маппинг страницы не порождал N+1.
    @Override
    @EntityGraph(attributePaths = {"user", "room.location", "workplace.location", "tariff", "payment"})
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.room.id = :roomId
              AND b.status <> kz.nurlybek.coworking_booking_api.model.enums.BookingStatus.CANCELLED
              AND b.startTime < :to
              AND b.endTime > :from
            """)
    boolean existsOverlapForRoom(@Param("roomId") Long roomId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.workplace.id = :workplaceId
              AND b.status <> kz.nurlybek.coworking_booking_api.model.enums.BookingStatus.CANCELLED
              AND b.startTime < :to
              AND b.endTime > :from
            """)
    boolean existsOverlapForWorkplace(@Param("workplaceId") Long workplaceId,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);

    boolean existsByUserIdAndRoomIdAndStatus(Long userId, Long roomId, BookingStatus status);

    /** Неотменённые брони, начинающиеся в периоде [from, to), с данными для отчётов. */
    @EntityGraph(attributePaths = {"room.location", "workplace.location", "payment"})
    @Query("""
            SELECT b FROM Booking b
            WHERE b.status <> kz.nurlybek.coworking_booking_api.model.enums.BookingStatus.CANCELLED
              AND b.startTime >= :from
              AND b.startTime < :to
            """)
    List<Booking> findActiveStartingBetween(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query("""
            SELECT b.status AS status, COUNT(b) AS count FROM Booking b
            WHERE b.startTime >= :from AND b.startTime < :to
            GROUP BY b.status
            """)
    List<StatusCount> countByStatusBetween(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    interface StatusCount {
        BookingStatus getStatus();

        long getCount();
    }
}
