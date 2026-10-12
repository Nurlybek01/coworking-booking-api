package kz.nurlybek.coworking_booking_api.controller;

import jakarta.validation.Valid;
import kz.nurlybek.coworking_booking_api.dto.LocationRequest;
import kz.nurlybek.coworking_booking_api.dto.LocationResponse;
import kz.nurlybek.coworking_booking_api.dto.PageResponse;
import kz.nurlybek.coworking_booking_api.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public PageResponse<LocationResponse> findAll(@RequestParam(required = false) String city,
                                                  @PageableDefault(sort = "name") Pageable pageable) {
        return PageResponse.from(locationService.findActive(city, pageable));
    }

    @GetMapping("/{id}")
    public LocationResponse findById(@PathVariable Long id) {
        return locationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public LocationResponse create(@Valid @RequestBody LocationRequest request) {
        return locationService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public LocationResponse update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deactivate(@PathVariable Long id) {
        locationService.deactivate(id);
    }
}
