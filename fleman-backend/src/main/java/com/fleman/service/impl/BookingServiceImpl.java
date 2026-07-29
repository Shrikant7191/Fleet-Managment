package com.fleman.service.impl;

import com.fleman.dto.*;
import com.fleman.entity.*;
import com.fleman.entity.BookingHeader.BookingStatus;
import com.fleman.entity.Car.CarStatus;
import com.fleman.exception.ApiException;
import com.fleman.exception.ResourceNotFoundException;
import com.fleman.repository.*;
import com.fleman.service.AddonService;
import com.fleman.service.BookingService;
import com.fleman.service.CustomerService;
import com.fleman.service.LocationService;
import com.fleman.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingHeaderRepository bookingHeaderRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CarRepository carRepository;
    private final CarTypeRepository carTypeRepository;
    private final CustomerRepository customerRepository;
    private final HubRepository hubRepository;
    private final AddonRepository addonRepository;

    // Reusing the sibling services' own DTO builders (getCarById, getHubById,
    // etc.) instead of duplicating that mapping logic here — one source of
    // truth per entity type.
    private final VehicleService vehicleService;
    private final CustomerService customerService;
    private final LocationService locationService;
    private final AddonService addonService;

    public BookingServiceImpl(BookingHeaderRepository bookingHeaderRepository,
                               BookingDetailRepository bookingDetailRepository,
                               CarRepository carRepository,
                               CarTypeRepository carTypeRepository,
                               CustomerRepository customerRepository,
                               HubRepository hubRepository,
                               AddonRepository addonRepository,
                               VehicleService vehicleService,
                               CustomerService customerService,
                               LocationService locationService,
                               AddonService addonService) {
        this.bookingHeaderRepository = bookingHeaderRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.carRepository = carRepository;
        this.carTypeRepository = carTypeRepository;
        this.customerRepository = customerRepository;
        this.hubRepository = hubRepository;
        this.addonRepository = addonRepository;
        this.vehicleService = vehicleService;
        this.customerService = customerService;
        this.locationService = locationService;
        this.addonService = addonService;
    }

    @Override
    @Transactional
    public BookingResponseDTO createBooking(CreateBookingRequest request) {
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ApiException("Selected vehicle is no longer available", 409));
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new ApiException("Selected vehicle is no longer available", 409);
        }
        if (!request.getReturnDatetime().isAfter(request.getPickupDatetime())) {
            throw new ApiException("Return date must be after pickup date", 400);
        }
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new ResourceNotFoundException("Customer not found: " + request.getCustomerId());
        }
        Long dropHubId = request.getDropHubId() != null ? request.getDropHubId() : request.getPickupHubId();
        if (!hubRepository.existsById(request.getPickupHubId())) {
            throw new ResourceNotFoundException("Hub not found: " + request.getPickupHubId());
        }
        if (!hubRepository.existsById(dropHubId)) {
            throw new ResourceNotFoundException("Hub not found: " + dropHubId);
        }

        BookingHeader header = new BookingHeader();
        header.setConfirmationNo(generateConfirmationNo());
        header.setBookingDate(LocalDateTime.now());
        header.setCustomerId(request.getCustomerId());
        header.setCarId(car.getCarId());
        header.setPickupHubId(request.getPickupHubId());
        header.setDropHubId(dropHubId);
        header.setPickupDatetime(request.getPickupDatetime());
        header.setReturnDatetime(request.getReturnDatetime());
        header.setBookingStatus(BookingStatus.CONFIRMED);
        header.setRemarks(request.getRemarks() != null ? request.getRemarks() : "");

        int days = daysBetween(request.getPickupDatetime(), request.getReturnDatetime());
        double dailyRate = carTypeRepository.findById(car.getCarTypeId())
                .map(CarType::getDailyRate).orElse(0.0);
        double rentalAmount = dailyRate * days;

        header = bookingHeaderRepository.save(header);

        double addonAmount = 0;
        if (request.getAddons() != null) {
            for (AddonLineRequest line : request.getAddons()) {
                Addon addon = addonRepository.findById(line.getAddonId()).orElse(null);
                if (addon == null) continue;
                // Subtotal is computed server-side from the addon's current
                // rate — the client never gets to submit its own price.
                double subtotal = addon.getDailyRate() * line.getQuantity() * days;
                BookingDetail detail = new BookingDetail();
                detail.setBookingId(header.getBookingId());
                detail.setAddonId(addon.getAddonId());
                detail.setAddonRate(addon.getDailyRate());
                detail.setQuantity(line.getQuantity());
                detail.setSubtotal(subtotal);
                bookingDetailRepository.save(detail);
                addonAmount += subtotal;
            }
        }
        header.setEstimatedAmount(rentalAmount + addonAmount);
        header = bookingHeaderRepository.save(header);

        car.setStatus(CarStatus.BOOKED);
        car.setAvailable(false);
        carRepository.save(car);

        return toResponseDto(header);
    }

    @Override
    public BookingResponseDTO getBookingByConfirmation(String confirmationNo) {
        BookingHeader header = findHeader(confirmationNo);
        return toResponseDto(header);
    }

    @Override
    public BookingResponseDTO getLastBookingForCustomer(Long customerId) {
        List<BookingHeader> mine = bookingHeaderRepository
                .findByCustomerIdAndBookingStatusNotOrderByBookingDateDesc(customerId, BookingStatus.CANCELLED);
        return mine.isEmpty() ? null : toResponseDto(mine.get(0));
    }

    @Override
    public List<BookingResponseDTO> getBookingsForCustomer(Long customerId) {
        // Same query as getLastBookingForCustomer, minus the .get(0) — used
        // by the "modify booking" flow's booking picker, which needs every
        // one of the customer's bookings, not just the most recent.
        return bookingHeaderRepository
                .findByCustomerIdAndBookingStatusNotOrderByBookingDateDesc(customerId, BookingStatus.CANCELLED)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponseDTO modifyBooking(String confirmationNo, ModifyBookingRequest request) {
        BookingHeader header = findHeader(confirmationNo);

        if (request.getPickupHubId() != null) {
            if (!hubRepository.existsById(request.getPickupHubId())) {
                throw new ResourceNotFoundException("Hub not found: " + request.getPickupHubId());
            }
            header.setPickupHubId(request.getPickupHubId());
        }
        if (request.getDropHubId() != null) {
            if (!hubRepository.existsById(request.getDropHubId())) {
                throw new ResourceNotFoundException("Hub not found: " + request.getDropHubId());
            }
            header.setDropHubId(request.getDropHubId());
        }
        if (request.getPickupDatetime() != null) header.setPickupDatetime(request.getPickupDatetime());
        if (request.getReturnDatetime() != null) header.setReturnDatetime(request.getReturnDatetime());
        if (request.getRemarks() != null) header.setRemarks(request.getRemarks());

        // Swapping the vehicle: release the old car back to the fleet and
        // claim the new one, the same availability check createBooking
        // itself uses - a modify request can't silently double-book a car
        // any more than a fresh booking can.
        if (request.getCarId() != null && !request.getCarId().equals(header.getCarId())) {
            Car newCar = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new ApiException("Selected vehicle is no longer available", 409));
            if (newCar.getStatus() != CarStatus.AVAILABLE) {
                throw new ApiException("Selected vehicle is no longer available", 409);
            }
            carRepository.findById(header.getCarId()).ifPresent(oldCar -> {
                oldCar.setStatus(CarStatus.AVAILABLE);
                oldCar.setAvailable(true);
                carRepository.save(oldCar);
            });
            newCar.setStatus(CarStatus.BOOKED);
            newCar.setAvailable(false);
            carRepository.save(newCar);
            header.setCarId(newCar.getCarId());
        }

        // Replacing the add-on lines: null on the request means "the
        // customer didn't touch this step, leave them as they are"; an
        // empty list means "every add-on was removed" - ordinary partial-
        // patch semantics, just applied to a whole child table instead of
        // a single column.
        int days = daysBetween(header.getPickupDatetime(), header.getReturnDatetime());
        double addonAmount;
        if (request.getAddons() != null) {
            bookingDetailRepository.deleteAll(bookingDetailRepository.findByBookingId(header.getBookingId()));
            double total = 0;
            for (AddonLineRequest line : request.getAddons()) {
                Addon addon = addonRepository.findById(line.getAddonId()).orElse(null);
                if (addon == null) continue;
                double subtotal = addon.getDailyRate() * line.getQuantity() * days;
                BookingDetail detail = new BookingDetail();
                detail.setBookingId(header.getBookingId());
                detail.setAddonId(addon.getAddonId());
                detail.setAddonRate(addon.getDailyRate());
                detail.setQuantity(line.getQuantity());
                detail.setSubtotal(subtotal);
                bookingDetailRepository.save(detail);
                total += subtotal;
            }
            addonAmount = total;
        } else {
            addonAmount = bookingDetailRepository.findByBookingId(header.getBookingId())
                    .stream().mapToDouble(BookingDetail::getSubtotal).sum();
        }

        double dailyRate = carTypeRepository.findById(
                        carRepository.findById(header.getCarId()).map(Car::getCarTypeId).orElse(null))
                .map(CarType::getDailyRate).orElse(0.0);
        header.setEstimatedAmount(dailyRate * days + addonAmount);

        return toResponseDto(bookingHeaderRepository.save(header));
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(String confirmationNo) {
        BookingHeader header = findHeader(confirmationNo);
        header.setBookingStatus(BookingStatus.CANCELLED);
        bookingHeaderRepository.save(header);

        Car car = carRepository.findById(header.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + header.getCarId()));
        car.setStatus(CarStatus.AVAILABLE);
        car.setAvailable(true);
        carRepository.save(car);

        return toResponseDto(header);
    }

    private BookingHeader findHeader(String confirmationNo) {
        return bookingHeaderRepository.findByConfirmationNoIgnoreCase(confirmationNo)
                .orElseThrow(() -> new ResourceNotFoundException("No booking found for that confirmation number"));
    }

    // Robust against deleted rows — scans existing confirmation numbers for
    // the highest "WDR-####" suffix rather than trusting a running count,
    // which a delete could throw out of sync.
    private String generateConfirmationNo() {
        int max = bookingHeaderRepository.findAll().stream()
                .map(BookingHeader::getConfirmationNo)
                .filter(no -> no != null && no.startsWith("WDR-"))
                .mapToInt(no -> {
                    try {
                        return Integer.parseInt(no.substring(4));
                    } catch (NumberFormatException e) {
                        return 2894;
                    }
                })
                .max().orElse(2894);
        return "WDR-" + (max + 1);
    }

    private int daysBetween(LocalDateTime pickup, LocalDateTime returnDt) {
        long hours = Duration.between(pickup, returnDt).toHours();
        long fullDays = (long) Math.ceil(hours / 24.0);
        return (int) Math.max(1, fullDays);
    }

    // The Java equivalent of the frontend mock's enrich(header) function —
    // every booking-related endpoint returns this same nested shape. No
    // entity relationships are traversed here — every nested object is
    // fetched by its plain id column through the owning service.
    private BookingResponseDTO toResponseDto(BookingHeader header) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setBookingId(header.getBookingId());
        dto.setConfirmationNo(header.getConfirmationNo());
        dto.setBookingDate(header.getBookingDate());
        dto.setCustomerId(header.getCustomerId());
        dto.setCarId(header.getCarId());
        dto.setPickupHubId(header.getPickupHubId());
        dto.setDropHubId(header.getDropHubId());
        dto.setPickupDatetime(header.getPickupDatetime());
        dto.setReturnDatetime(header.getReturnDatetime());
        dto.setBookingStatus(header.getBookingStatus() != null ? header.getBookingStatus().name() : null);
        dto.setEstimatedAmount(header.getEstimatedAmount());
        dto.setRemarks(header.getRemarks());

        dto.setCar(vehicleService.getCarById(header.getCarId()));
        dto.setCarType(dto.getCar().getCarType());
        dto.setCustomer(customerService.getCustomerById(header.getCustomerId()));
        dto.setPickupHub(locationService.getHubById(header.getPickupHubId()));
        dto.setDropHub(locationService.getHubById(header.getDropHubId()));

        List<AddonDTO> allAddons = addonService.getAddons();
        List<BookingDetailDTO> lines = bookingDetailRepository.findByBookingId(header.getBookingId())
                .stream()
                .map(detail -> {
                    BookingDetailDTO lineDto = new BookingDetailDTO();
                    lineDto.setBookingDetailId(detail.getBookingDetailId());
                    lineDto.setBookingId(header.getBookingId());
                    lineDto.setAddonId(detail.getAddonId());
                    lineDto.setAddonRate(detail.getAddonRate());
                    lineDto.setQuantity(detail.getQuantity());
                    lineDto.setSubtotal(detail.getSubtotal());
                    lineDto.setAddon(allAddons.stream()
                            .filter(a -> a.getAddonId().equals(detail.getAddonId()))
                            .findFirst().orElse(null));
                    return lineDto;
                })
                .collect(Collectors.toList());
        dto.setAddonLines(lines);

        return dto;
    }
}
