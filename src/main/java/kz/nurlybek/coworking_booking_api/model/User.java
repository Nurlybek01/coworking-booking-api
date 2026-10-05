package kz.nurlybek.coworking_booking_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.nurlybek.coworking_booking_api.model.enums.Role;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Email
    @Column(nullable = false, length = 120)
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    @Column(nullable = false)
    private String password;

    @NotBlank @Size(max = 80)
    @Column(name = "full_name", nullable = false, length = 80)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return id != null && id.equals(u.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}