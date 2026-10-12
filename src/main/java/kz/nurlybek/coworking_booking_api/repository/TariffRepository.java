package kz.nurlybek.coworking_booking_api.repository;

import kz.nurlybek.coworking_booking_api.model.Tariff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    Page<Tariff> findByActiveTrue(Pageable pageable);
}
