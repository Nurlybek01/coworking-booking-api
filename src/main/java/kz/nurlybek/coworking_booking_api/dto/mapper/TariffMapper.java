package kz.nurlybek.coworking_booking_api.dto.mapper;

import kz.nurlybek.coworking_booking_api.dto.TariffRequest;
import kz.nurlybek.coworking_booking_api.dto.TariffResponse;
import kz.nurlybek.coworking_booking_api.model.Tariff;

public final class TariffMapper {

    private TariffMapper() {
    }

    public static TariffResponse toResponse(Tariff tariff) {
        return new TariffResponse(tariff.getId(), tariff.getName(), tariff.getPricePerHour(),
                tariff.getDayOfWeek(), tariff.getValidFrom(), tariff.getValidTo(), tariff.isActive());
    }

    public static void updateEntity(Tariff tariff, TariffRequest request) {
        tariff.setName(request.name());
        tariff.setPricePerHour(request.pricePerHour());
        tariff.setDayOfWeek(request.dayOfWeek());
        tariff.setValidFrom(request.validFrom());
        tariff.setValidTo(request.validTo());
    }
}
