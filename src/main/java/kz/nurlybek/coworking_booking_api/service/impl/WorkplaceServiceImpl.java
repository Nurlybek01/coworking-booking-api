package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.dto.WorkplaceRequest;
import kz.nurlybek.coworking_booking_api.dto.WorkplaceResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.WorkplaceMapper;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.Workplace;
import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;
import kz.nurlybek.coworking_booking_api.repository.LocationRepository;
import kz.nurlybek.coworking_booking_api.repository.RoomRepository;
import kz.nurlybek.coworking_booking_api.repository.WorkplaceRepository;
import kz.nurlybek.coworking_booking_api.service.WorkplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkplaceServiceImpl implements WorkplaceService {

    private final WorkplaceRepository workplaceRepository;
    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;

    @Override
    public Page<WorkplaceResponse> search(Long locationId, WorkplaceType type, Pageable pageable) {
        return workplaceRepository.search(locationId, type, pageable).map(WorkplaceMapper::toResponse);
    }

    @Override
    public Page<WorkplaceResponse> findAvailable(LocalDateTime from, LocalDateTime to,
                                                 Long locationId, WorkplaceType type, Pageable pageable) {
        if (!to.isAfter(from)) {
            throw new BusinessRuleException("'to' must be after 'from'");
        }
        return workplaceRepository.findAvailable(from, to, locationId, type, pageable)
                .map(WorkplaceMapper::toResponse);
    }

    @Override
    public WorkplaceResponse findById(Long id) {
        return WorkplaceMapper.toResponse(getWorkplace(id));
    }

    @Override
    @Transactional
    public WorkplaceResponse create(WorkplaceRequest request) {
        ensureNumberIsFree(request.locationId(), request.number());
        Workplace workplace = new Workplace();
        apply(workplace, request);
        return WorkplaceMapper.toResponse(workplaceRepository.save(workplace));
    }

    @Override
    @Transactional
    public WorkplaceResponse update(Long id, WorkplaceRequest request) {
        Workplace workplace = getWorkplace(id);
        boolean numberChanged = !workplace.getNumber().equals(request.number())
                || !workplace.getLocation().getId().equals(request.locationId());
        if (numberChanged) {
            ensureNumberIsFree(request.locationId(), request.number());
        }
        apply(workplace, request);
        return WorkplaceMapper.toResponse(workplace);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        getWorkplace(id).setActive(false);
    }

    private void apply(Workplace workplace, WorkplaceRequest request) {
        Location location = locationRepository.findById(request.locationId())
                .filter(Location::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Location", request.locationId()));
        Room room = request.roomId() == null ? null : getRoomInLocation(request.roomId(), location);
        WorkplaceMapper.updateEntity(workplace, request, location, room);
    }

    private Room getRoomInLocation(Long roomId, Location location) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        if (!Objects.equals(room.getLocation().getId(), location.getId())) {
            throw new BusinessRuleException("Room %d does not belong to location %d".formatted(roomId, location.getId()));
        }
        return room;
    }

    private void ensureNumberIsFree(Long locationId, String number) {
        if (workplaceRepository.existsByLocationIdAndNumber(locationId, number)) {
            throw new ConflictException("Workplace number '%s' already exists in this location".formatted(number));
        }
    }

    private Workplace getWorkplace(Long id) {
        return workplaceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Workplace", id));
    }
}
