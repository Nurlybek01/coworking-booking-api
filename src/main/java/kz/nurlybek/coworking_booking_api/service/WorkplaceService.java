package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.WorkplaceRequest;
import kz.nurlybek.coworking_booking_api.dto.WorkplaceResponse;
import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface WorkplaceService {

    Page<WorkplaceResponse> search(Long locationId, WorkplaceType type, Pageable pageable);

    Page<WorkplaceResponse> findAvailable(LocalDateTime from, LocalDateTime to,
                                          Long locationId, WorkplaceType type, Pageable pageable);

    WorkplaceResponse findById(Long id);

    WorkplaceResponse create(WorkplaceRequest request);

    WorkplaceResponse update(Long id, WorkplaceRequest request);

    void deactivate(Long id);
}
