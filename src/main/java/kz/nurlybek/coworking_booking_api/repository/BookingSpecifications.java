package kz.nurlybek.coworking_booking_api.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import kz.nurlybek.coworking_booking_api.dto.BookingFilter;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.Workplace;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<Booking> matching(BookingFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }
            if (filter.roomId() != null) {
                predicates.add(cb.equal(root.get("room").get("id"), filter.roomId()));
            }
            if (filter.workplaceId() != null) {
                predicates.add(cb.equal(root.get("workplace").get("id"), filter.workplaceId()));
            }
            if (filter.locationId() != null) {
                // Локация брони — через комнату или через рабочее место; join'ы LEFT, т.к. одно из них null.
                Join<Booking, Room> room = root.join("room", JoinType.LEFT);
                Join<Booking, Workplace> workplace = root.join("workplace", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.equal(room.get("location").get("id"), filter.locationId()),
                        cb.equal(workplace.get("location").get("id"), filter.locationId())));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThan(root.get("startTime"), filter.to()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
