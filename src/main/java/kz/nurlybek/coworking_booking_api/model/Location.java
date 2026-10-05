package kz.nurlybek.coworking_booking_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String address;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String city;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location l)) return false;
        return id != null && id.equals(l.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}