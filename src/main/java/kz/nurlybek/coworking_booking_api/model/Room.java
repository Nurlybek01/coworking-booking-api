package kz.nurlybek.coworking_booking_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;

    @NotNull @Min(1)
    @Column(nullable = false)
    private Integer capacity;

    @NotNull
    @Column(name = "hourly_base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyBasePrice;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_room_location"))
    private Location location;

    @OneToMany(mappedBy = "room")
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}