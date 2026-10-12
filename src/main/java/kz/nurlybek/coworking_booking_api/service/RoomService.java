package kz.nurlybek.coworking_booking_api.service;

import kz.nurlybek.coworking_booking_api.dto.RoomRequest;
import kz.nurlybek.coworking_booking_api.dto.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface RoomService {

    Page<RoomResponse> search(Long locationId, Integer minCapacity, Pageable pageable);

    Page<RoomResponse> findAvailable(LocalDateTime from, LocalDateTime to,
                                     Long locationId, Integer minCapacity, Pageable pageable);

    RoomResponse findById(Long id);

    RoomResponse create(RoomRequest request);

    RoomResponse update(Long id, RoomRequest request);

    void deactivate(Long id);
}
