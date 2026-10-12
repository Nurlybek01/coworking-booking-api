package kz.nurlybek.coworking_booking_api.controller;

import jakarta.validation.Valid;
import kz.nurlybek.coworking_booking_api.dto.PageResponse;
import kz.nurlybek.coworking_booking_api.dto.WorkplaceRequest;
import kz.nurlybek.coworking_booking_api.dto.WorkplaceResponse;
import kz.nurlybek.coworking_booking_api.model.enums.WorkplaceType;
import kz.nurlybek.coworking_booking_api.service.WorkplaceService;
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
@RequestMapping("/api/workplaces")
@RequiredArgsConstructor
public class WorkplaceController {

    private final WorkplaceService workplaceService;

    @GetMapping
    public PageResponse<WorkplaceResponse> search(@RequestParam(required = false) Long locationId,
                                                  @RequestParam(required = false) WorkplaceType type,
                                                  @PageableDefault(sort = "number") Pageable pageable) {
        return PageResponse.from(workplaceService.search(locationId, type, pageable));
    }

    @GetMapping("/available")
    public PageResponse<WorkplaceResponse> findAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) WorkplaceType type,
            @PageableDefault(sort = "number") Pageable pageable) {
        return PageResponse.from(workplaceService.findAvailable(from, to, locationId, type, pageable));
    }

    @GetMapping("/{id}")
    public WorkplaceResponse findById(@PathVariable Long id) {
        return workplaceService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public WorkplaceResponse create(@Valid @RequestBody WorkplaceRequest request) {
        return workplaceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkplaceResponse update(@PathVariable Long id, @Valid @RequestBody WorkplaceRequest request) {
        return workplaceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable Long id) {
        workplaceService.deactivate(id);
    }
}
