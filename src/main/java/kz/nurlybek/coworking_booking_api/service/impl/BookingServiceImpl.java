package kz.nurlybek.coworking_booking_api.service.impl;

import kz.nurlybek.coworking_booking_api.audit.AuditAction;
import kz.nurlybek.coworking_booking_api.audit.AuditLogger;
import kz.nurlybek.coworking_booking_api.dto.BookingFilter;
import kz.nurlybek.coworking_booking_api.dto.BookingRequest;
import kz.nurlybek.coworking_booking_api.dto.BookingResponse;
import kz.nurlybek.coworking_booking_api.dto.mapper.BookingMapper;
import kz.nurlybek.coworking_booking_api.exception.BusinessRuleException;
import kz.nurlybek.coworking_booking_api.exception.ConflictException;
import kz.nurlybek.coworking_booking_api.exception.ResourceNotFoundException;
import kz.nurlybek.coworking_booking_api.model.Booking;
import kz.nurlybek.coworking_booking_api.model.Room;
import kz.nurlybek.coworking_booking_api.model.Service;
import kz.nurlybek.coworking_booking_api.model.Tariff;
import kz.nurlybek.coworking_booking_api.model.Workplace;
import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import kz.nurlybek.coworking_booking_api.repository.BookingRepository;
import kz.nurlybek.coworking_booking_api.repository.BookingSpecifications;
import kz.nurlybek.coworking_booking_api.repository.RoomRepository;
import kz.nurlybek.coworking_booking_api.repository.ServiceRepository;
import kz.nurlybek.coworking_booking_api.repository.TariffRepository;
import kz.nurlybek.coworking_booking_api.repository.UserRepository;
import kz.nurlybek.coworking_booking_api.repository.WorkplaceRepository;
import kz.nurlybek.coworking_booking_api.security.AuthUser;
import kz.nurlybek.coworking_booking_api.service.BookingPriceCalculator;
import kz.nurlybek.coworking_booking_api.service.BookingService;
import kz.nurlybek.coworking_booking_api.service.BookingTimePolicy;
import kz.nurlybek.coworking_booking_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Аннотация указана полным именем: простое имя Service занято сущностью доп. услуги.
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Set<BookingStatus> CANCELLABLE = Set.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final WorkplaceRepository workplaceRepository;
    private final TariffRepository tariffRepository;
    private final ServiceRepository serviceRepository;
    private final BookingTimePolicy timePolicy;
    private final BookingPriceCalculator priceCalculator;
    private final PaymentService paymentService;
    private final BookingAccess bookingAccess;
    private final AuditLogger auditLogger;
    private final Clock clock;

    @Override
    @Transactional
    public BookingResponse create(AuthUser actor, BookingRequest request) {
        LocalDateTime start = request.startTime();
        LocalDateTime end = request.endTime();

        Booking booking = Booking.builder()
                .user(userRepository.getReferenceById(actor.id()))
                .startTime(start)
                .endTime(end)
                .status(BookingStatus.PENDING)
                .notes(request.notes())
                .build();

        BigDecimal baseRate = request.roomId() != null
                ? attachRoom(booking, request.roomId())
                : attachWorkplace(booking, request.workplaceId());

        Tariff tariff = request.tariffId() == null ? null : getTariff(request.tariffId());
        Set<Service> services = resolveServices(request.serviceIdsOrEmpty());
        booking.setTariff(tariff);
        booking.setServices(services);
        booking.setTotalPrice(priceCalculator.calculate(baseRate, tariff, start, end, services));

        Booking saved = bookingRepository.save(booking);
        auditLogger.log(AuditAction.BOOKING_CREATED, actor.email(),
                "bookingId=%d %s total=%s".formatted(saved.getId(), resourceLabel(saved), saved.getTotalPrice()));
        return BookingMapper.toResponse(saved);
    }

    @Override
    public BookingResponse findById(AuthUser actor, Long id) {
        return BookingMapper.toResponse(bookingAccess.getAccessible(actor, id));
    }

    @Override
    public Page<BookingResponse> search(AuthUser actor, BookingFilter filter, Pageable pageable) {
        BookingFilter effective = actor.isStaff() ? filter : filter.withUserId(actor.id());
        return bookingRepository.findAll(BookingSpecifications.matching(effective), pageable)
                .map(BookingMapper::toResponse);
    }

    @Override
    @Transactional
    public BookingResponse cancel(AuthUser actor, Long id) {
        Booking booking = bookingAccess.getAccessible(actor, id);
        if (!CANCELLABLE.contains(booking.getStatus())) {
            throw new ConflictException("Booking in status %s cannot be cancelled".formatted(booking.getStatus()));
        }
        if (!actor.isStaff() && !booking.getStartTime().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("A booking that has already started can be cancelled only by staff");
        }

        paymentService.refundIfPaid(actor, booking);
        booking.setStatus(BookingStatus.CANCELLED);
        auditLogger.log(AuditAction.BOOKING_CANCELLED, actor.email(), "bookingId=" + id);
        return BookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse complete(AuthUser actor, Long id) {
        Booking booking = bookingAccess.getAccessible(actor, id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed bookings can be completed");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        auditLogger.log(AuditAction.BOOKING_COMPLETED, actor.email(), "bookingId=" + id);
        return BookingMapper.toResponse(booking);
    }

    /** Возвращает базовую почасовую ставку комнаты. */
    private BigDecimal attachRoom(Booking booking, Long roomId) {
        Room room = roomRepository.findByIdForUpdate(roomId)
                .filter(r -> r.isActive() && r.getLocation().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        timePolicy.validate(booking.getStartTime(), booking.getEndTime(), room.getLocation());
        if (bookingRepository.existsOverlapForRoom(roomId, booking.getStartTime(), booking.getEndTime())) {
            throw new ConflictException("Room is already booked for the selected time");
        }
        booking.setRoom(room);
        return room.getHourlyBasePrice();
    }

    /** Возвращает базовую почасовую ставку рабочего места. */
    private BigDecimal attachWorkplace(Booking booking, Long workplaceId) {
        Workplace workplace = workplaceRepository.findByIdForUpdate(workplaceId)
                .filter(w -> w.isActive() && w.getLocation().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Workplace", workplaceId));
        timePolicy.validate(booking.getStartTime(), booking.getEndTime(), workplace.getLocation());
        if (bookingRepository.existsOverlapForWorkplace(workplaceId, booking.getStartTime(), booking.getEndTime())) {
            throw new ConflictException("Workplace is already booked for the selected time");
        }
        booking.setWorkplace(workplace);
        return workplace.getHourlyBasePrice();
    }

    private Tariff getTariff(Long id) {
        return tariffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tariff", id));
    }

    private Set<Service> resolveServices(Set<Long> ids) {
        if (ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Service> found = serviceRepository.findByIdInAndActiveTrue(ids);
        if (found.size() != ids.size()) {
            Set<Long> missing = new HashSet<>(ids);
            found.forEach(s -> missing.remove(s.getId()));
            throw new BusinessRuleException("Unknown or inactive services: " + missing);
        }
        return new HashSet<>(found);
    }

    private static String resourceLabel(Booking booking) {
        return booking.getRoom() != null
                ? "roomId=" + booking.getRoom().getId()
                : "workplaceId=" + booking.getWorkplace().getId();
    }
}
