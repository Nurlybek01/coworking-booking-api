package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.dto.RoomRequest;
import kz.nurlybek.coworking_booking_api.dto.RoomResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.RoomMapper;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.repository.LocationRepository;
import kz.nurlybek.coworking_booking_api.repository.RoomRepository;
import kz.nurlybek.coworking_booking_api.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final LocationRepository locationRepository;

    @Override
    public Page<RoomResponse> search(Long locationId, Integer minCapacity, Pageable pageable) {
        return roomRepository.search(locationId, minCapacity, pageable).map(RoomMapper::toResponse);
    }

    @Override
    public Page<RoomResponse> findAvailable(LocalDateTime from, LocalDateTime to,
                                            Long locationId, Integer minCapacity, Pageable pageable) {
        if (!to.isAfter(from)) {
            throw new BusinessRuleException("'to' must be after 'from'");
        }
        return roomRepository.findAvailable(from, to, locationId, minCapacity, pageable).map(RoomMapper::toResponse);
    }

    @Override
    public RoomResponse findById(Long id) {
        return RoomMapper.toResponse(getRoom(id));
    }

    @Override
    @Transactional
    public RoomResponse create(RoomRequest request) {
        Room room = new Room();
        RoomMapper.updateEntity(room, request, getActiveLocation(request.locationId()));
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = getRoom(id);
        RoomMapper.updateEntity(room, request, getActiveLocation(request.locationId()));
        return RoomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        getRoom(id).setActive(false);
    }

    private Room getRoom(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    private Location getActiveLocation(Long id) {
        return locationRepository.findById(id)
                .filter(Location::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
    }
}
