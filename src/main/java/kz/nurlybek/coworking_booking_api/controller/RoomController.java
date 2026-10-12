package kz.nurlybek.coworking_booking_api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import kz.nurlybek.coworking_booking_api.dto.PageResponse;
import kz.nurlybek.coworking_booking_api.dto.RoomRequest;
import kz.nurlybek.coworking_booking_api.dto.RoomResponse;
import kz.nurlybek.coworking_booking_api.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public PageResponse<RoomResponse> search(@RequestParam(required = false) Long locationId,
                                             @RequestParam(required = false) @Min(1) Integer minCapacity,
                                             @PageableDefault(sort = "name") Pageable pageable) {
        return PageResponse.from(roomService.search(locationId, minCapacity, pageable));
    }

    @GetMapping("/available")
    public PageResponse<RoomResponse> findAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) @Min(1) Integer minCapacity,
            @PageableDefault(sort = "name") Pageable pageable) {
        return PageResponse.from(roomService.findAvailable(from, to, locationId, minCapacity, pageable));
    }

    @GetMapping("/{id}")
    public RoomResponse findById(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse create(@Valid @RequestBody RoomRequest request) {
        return roomService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable Long id) {
        roomService.deactivate(id);
    }
}
