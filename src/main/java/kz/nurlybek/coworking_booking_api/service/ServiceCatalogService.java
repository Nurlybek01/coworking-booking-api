package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.ServiceRequest;
import kz.nurlybek.coworking_booking_api.dto.ServiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Каталог дополнительных услуг (сущность Service). */
public interface ServiceCatalogService {

    Page<ServiceResponse> findActive(Pageable pageable);

    ServiceResponse findById(Long id);

    ServiceResponse create(ServiceRequest request);

    ServiceResponse update(Long id, ServiceRequest request);

    void deactivate(Long id);
}
