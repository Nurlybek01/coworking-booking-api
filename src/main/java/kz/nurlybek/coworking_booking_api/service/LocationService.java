package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.LocationRequest;
import kz.nurlybek.coworking_booking_api.dto.LocationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationService {

    Page<LocationResponse> findActive(String city, Pageable pageable);

    LocationResponse findById(Long id);

    LocationResponse create(LocationRequest request);

    LocationResponse update(Long id, LocationRequest request);

    /** Мягкое удаление: на локацию ссылаются брони, поэтому она только деактивируется. */
    void deactivate(Long id);
}
