package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.dto.ServiceRequest;
import kz.nurlybek.coworking_booking_api.dto.ServiceResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.ServiceMapper;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Service;
import kz.nurlybek.coworking_booking_api.repository.ServiceRepository;
import kz.nurlybek.coworking_booking_api.service.ServiceCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

// Аннотация указана полным именем: простое имя Service занято сущностью доп. услуги.
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceCatalogServiceImpl implements ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    @Override
    public Page<ServiceResponse> findActive(Pageable pageable) {
        return serviceRepository.findByActiveTrue(pageable).map(ServiceMapper::toResponse);
    }

    @Override
    public ServiceResponse findById(Long id) {
        return ServiceMapper.toResponse(getService(id));
    }

    @Override
    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        Service service = new Service();
        ServiceMapper.updateEntity(service, request);
        return ServiceMapper.toResponse(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public ServiceResponse update(Long id, ServiceRequest request) {
        Service service = getService(id);
        ServiceMapper.updateEntity(service, request);
        return ServiceMapper.toResponse(service);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        getService(id).setActive(false);
    }

    private Service getService(Long id) {
        return serviceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service", id));
    }
}
