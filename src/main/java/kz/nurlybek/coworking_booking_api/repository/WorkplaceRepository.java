package kz.nurlybek.coworking_booking_api.repository;

import jakarta.persistence.LockModeType;
import kz.nurlybek.coworking_booking_api.model.Workplace;
import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WorkplaceRepository extends JpaRepository<Workplace, Long> {

    boolean existsByLocationIdAndNumber(Long locationId, String number);

    @Query("""
            SELECT w FROM Workplace w
            WHERE w.active = true
              AND (:locationId IS NULL OR w.location.id = :locationId)
              AND (:type IS NULL OR w.type = :type)
            """)
    Page<Workplace> search(@Param("locationId") Long locationId,
                           @Param("type") WorkplaceType type,
                           Pageable pageable);

    // Блокировка строки ресурса сериализует параллельные брони одного места,
    // иначе две транзакции могут одновременно пройти проверку пересечений.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Workplace w WHERE w.id = :id")
    Optional<Workplace> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT w FROM Workplace w
            WHERE w.active = true
              AND (:locationId IS NULL OR w.location.id = :locationId)
              AND (:type IS NULL OR w.type = :type)
              AND NOT EXISTS (
                  SELECT 1 FROM Booking b
                  WHERE b.workplace = w
                    AND b.status <> kz.nurlybek.coworking_booking_api.model.enums.BookingStatus.CANCELLED
                    AND b.startTime < :to
                    AND b.endTime > :from
              )
            """)
    Page<Workplace> findAvailable(@Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to,
                                  @Param("locationId") Long locationId,
                                  @Param("type") WorkplaceType type,
                                  Pageable pageable);
}
