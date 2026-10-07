package kz.nurlybek.coworking_booking_api.repository;

import kz.nurlybek.coworking_booking_api.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    List<Room> findByLocationId(Long locationId);

    Page<Room> findByLocationIdAndActiveTrue(Long locationId, Pageable pageable);

    @Query("""
            SELECT r FROM Room r
            WHERE r.active = true
              AND (:locationId IS NULL OR r.location.id = :locationId)
              AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)
              AND NOT EXISTS (
                  SELECT 1 FROM Booking b
                  WHERE b.room = r
                    AND b.status <> kz.nurlybek.coworking_booking_api.model.enums.BookingStatus.CANCELLED
                    AND b.startTime < :to
                    AND b.endTime > :from
              )
            """)
    Page<Room> findAvailable(@Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to,
                             @Param("locationId") Long locationId,
                             @Param("minCapacity") Integer minCapacity,
                             Pageable pageable);
}