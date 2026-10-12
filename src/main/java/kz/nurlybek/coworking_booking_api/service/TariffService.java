package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.TariffRequest;
import kz.nurlybek.coworking_booking_api.dto.TariffResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TariffService {

    Page<TariffResponse> findActive(Pageable pageable);

    TariffResponse findById(Long id);

    TariffResponse create(TariffRequest request);

    TariffResponse update(Long id, TariffRequest request);

    void deactivate(Long id);
}
