package kz.nurlybek.coworking_booking_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user", columnList = "user_id"),
        @Index(name = "idx_booking_time", columnList = "start_time, end_time")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_booking_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", foreignKey = @ForeignKey(name = "fk_booking_room"))
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_id", foreignKey = @ForeignKey(name = "fk_booking_workplace"))
    private Workplace workplace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", foreignKey = @ForeignKey(name = "fk_booking_tariff"))
    private Tariff tariff;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "booking_services",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_booking_service",
                    columnNames = {"booking_id", "service_id"}))
    @Builder.Default
    private Set<Service> services = new HashSet<>();

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;

    @PrePersist @PreUpdate
    private void validateInvariants() {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalStateException("end_time must be strictly after start_time");
        }
        if (room == null && workplace == null) {
            throw new IllegalStateException("Booking must reference either room or workplace");
        }
        if (room != null && workplace != null) {
            throw new IllegalStateException("Booking cannot reference both room and workplace");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking b)) return false;
        return id != null && id.equals(b.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}