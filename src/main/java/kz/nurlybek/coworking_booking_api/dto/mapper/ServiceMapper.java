package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.ServiceRequest;
import kz.nurlybek.coworking_booking_api.dto.ServiceResponse;
import kz.nurlybek.coworking_booking_api.model.Service;

public final class ServiceMapper {

    private ServiceMapper() {
    }

    public static ServiceResponse toResponse(Service service) {
        return new ServiceResponse(service.getId(), service.getName(), service.getDescription(),
                service.getPrice(), service.isActive());
    }

    public static void updateEntity(Service service, ServiceRequest request) {
        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
    }
}
