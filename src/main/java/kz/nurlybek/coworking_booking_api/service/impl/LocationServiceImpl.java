package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.dto.LocationRequest;
import kz.nurlybek.coworking_booking_api.dto.LocationResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.LocationMapper;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Location;
import kz.nurlybek.coworking_booking_api.repository.LocationRepository;
import kz.nurlybek.coworking_booking_api.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public Page<LocationResponse> findActive(String city, Pageable pageable) {
        Page<Location> locations = StringUtils.hasText(city)
                ? locationRepository.findByCityIgnoreCaseAndActiveTrue(city.trim(), pageable)
                : locationRepository.findByActiveTrue(pageable);
        return locations.map(LocationMapper::toResponse);
    }

    @Override
    public LocationResponse findById(Long id) {
        return LocationMapper.toResponse(getLocation(id));
    }

    @Override
    @Transactional
    public LocationResponse create(LocationRequest request) {
        Location location = new Location();
        LocationMapper.updateEntity(location, request);
        return LocationMapper.toResponse(locationRepository.save(location));
    }

    @Override
    @Transactional
    public LocationResponse update(Long id, LocationRequest request) {
        Location location = getLocation(id);
        LocationMapper.updateEntity(location, request);
        return LocationMapper.toResponse(location);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        getLocation(id).setActive(false);
    }

    private Location getLocation(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", id));
    }
}
