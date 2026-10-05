package kz.nurlybek.coworking_booking_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "workplaces",
        uniqueConstraints = @UniqueConstraint(name = "uk_workplace_loc_number",
                columnNames = {"location_id", "number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Workplace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String number;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkplaceType type;

    @NotNull
    @Column(name = "hourly_base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyBasePrice;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_workplace_location"))
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id",
            foreignKey = @ForeignKey(name = "fk_workplace_room"))
    private Room room;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Workplace w)) return false;
        return id != null && id.equals(w.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}