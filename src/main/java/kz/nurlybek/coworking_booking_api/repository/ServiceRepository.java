package kz.nurlybek.coworking_booking_api.repository;

import kz.nurlybek.coworking_booking_api.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Page<Service> findByActiveTrue(Pageable pageable);

    List<Service> findByIdInAndActiveTrue(Collection<Long> ids);
}
