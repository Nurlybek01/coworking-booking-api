package kz.nurlybek.coworking_booking_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_user_room",
                columnNames = {"user_id", "room_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_user"))
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_room"))
    private Room room;

    @NotNull @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "text")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}