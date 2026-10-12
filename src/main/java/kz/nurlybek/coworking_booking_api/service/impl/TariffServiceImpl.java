package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.dto.TariffRequest;
import kz.nurlybek.coworking_booking_api.dto.TariffResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.TariffMapper;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Tariff;
import kz.nurlybek.coworking_booking_api.repository.TariffRepository;
import kz.nurlybek.coworking_booking_api.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TariffServiceImpl implements TariffService {

    private final TariffRepository tariffRepository;

    @Override
    public Page<TariffResponse> findActive(Pageable pageable) {
        return tariffRepository.findByActiveTrue(pageable).map(TariffMapper::toResponse);
    }

    @Override
    public TariffResponse findById(Long id) {
        return TariffMapper.toResponse(getTariff(id));
    }

    @Override
    @Transactional
    public TariffResponse create(TariffRequest request) {
        Tariff tariff = new Tariff();
        TariffMapper.updateEntity(tariff, request);
        return TariffMapper.toResponse(tariffRepository.save(tariff));
    }

    @Override
    @Transactional
    public TariffResponse update(Long id, TariffRequest request) {
        Tariff tariff = getTariff(id);
        TariffMapper.updateEntity(tariff, request);
        return TariffMapper.toResponse(tariff);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        getTariff(id).setActive(false);
    }

    private Tariff getTariff(Long id) {
        return tariffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tariff", id));
    }
}
