package com.fleman.service.impl;

import com.fleman.dto.BookingResponseDTO;
import com.fleman.dto.CarTypeAvailabilityDTO;
import com.fleman.dto.HandoverRequest;
import com.fleman.dto.InvoiceResponseDTO;
import com.fleman.dto.ProcessReturnRequest;
import com.fleman.entity.*;
import com.fleman.entity.BookingHeader.BookingStatus;
import com.fleman.entity.Car.CarStatus;
import com.fleman.entity.InvoiceHeader.PaymentStatus;
import com.fleman.exception.ResourceNotFoundException;
import com.fleman.repository.*;
import com.fleman.service.BookingService;
import com.fleman.service.StaffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {

    private final BookingHeaderRepository bookingHeaderRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CarRepository carRepository;
    private final CarTypeRepository carTypeRepository;
    private final CustomerRepository customerRepository;
    private final AddonRepository addonRepository;
    private final InvoiceHeaderRepository invoiceHeaderRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final BookingService bookingService;

    public StaffServiceImpl(BookingHeaderRepository bookingHeaderRepository,
                             BookingDetailRepository bookingDetailRepository,
                             CarRepository carRepository,
                             CarTypeRepository carTypeRepository,
                             CustomerRepository customerRepository,
                             AddonRepository addonRepository,
                             InvoiceHeaderRepository invoiceHeaderRepository,
                             InvoiceDetailRepository invoiceDetailRepository,
                             BookingService bookingService) {
        this.bookingHeaderRepository = bookingHeaderRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.carRepository = carRepository;
        this.carTypeRepository = carTypeRepository;
        this.customerRepository = customerRepository;
        this.addonRepository = addonRepository;
        this.invoiceHeaderRepository = invoiceHeaderRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.bookingService = bookingService;
    }

    @Override
    @Transactional
    public BookingResponseDTO handoverVehicle(String confirmationNo, HandoverRequest request) {
        BookingHeader header = findHeader(confirmationNo);
        Car car = carRepository.findById(header.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + header.getCarId()));
        car.setStatus(CarStatus.BOOKED);
        car.setAvailable(false);
        if (request.getFuelStatus() != null) car.setFuelLevel(request.getFuelStatus());
        carRepository.save(car);

        header.setBookingStatus(BookingStatus.ONGOING);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            String existing = header.getRemarks() != null ? header.getRemarks() : "";
            header.setRemarks((existing + " | Handover: " + request.getNotes()).trim());
        }
        bookingHeaderRepository.save(header);

        return bookingService.getBookingByConfirmation(confirmationNo);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO processReturn(String confirmationNo, ProcessReturnRequest request) {
        BookingHeader header = findHeader(confirmationNo);
        Car car = carRepository.findById(header.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + header.getCarId()));
        CarType carType = carTypeRepository.findById(car.getCarTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Car type not found: " + car.getCarTypeId()));
        Customer customer = customerRepository.findById(header.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + header.getCustomerId()));
        List<BookingDetail> lines = bookingDetailRepository.findByBookingId(header.getBookingId());

        int days = daysBetween(header.getPickupDatetime(), header.getReturnDatetime());
        double rentalAmount = carType.getDailyRate() * days;
        double addonAmount = lines.stream().mapToDouble(BookingDetail::getSubtotal).sum();
        double extraCharge = request.getExtraChargeAmount() != null ? request.getExtraChargeAmount() : 0.0;
        double totalAmount = rentalAmount + addonAmount + extraCharge;

        InvoiceHeader invoice = new InvoiceHeader();
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setBookingId(header.getBookingId());
        invoice.setCustomerName(customer.getFullName());
        invoice.setPhone(customer.getPhone());
        invoice.setEmail(customer.getEmail());
        invoice.setVehicleNumber(car.getVehicleNumber());
        invoice.setPickupHub(header.getPickupHubId());
        invoice.setDropHub(header.getDropHubId());
        invoice.setRentalAmount(rentalAmount);
        invoice.setAddonAmount(addonAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaymentType(request.getPaymentType());
        // Assumes payment is settled at the return desk when a payment
        // type is given; otherwise left PENDING for a separate collection step.
        invoice.setPaymentStatus(request.getPaymentType() != null ? PaymentStatus.PAID : PaymentStatus.PENDING);
        invoice.setExtraMiles(request.getExtraMiles());
        invoice.setExtraChargeAmount(extraCharge);
        invoice.setDamageNotes(request.getDamageNotes());
        invoice.setDays(days);
        invoice = invoiceHeaderRepository.save(invoice);
        invoice.setPaymentReference("PAY-" + invoice.getInvoiceId() + "-" + String.valueOf(System.currentTimeMillis()).substring(9));
        invoice = invoiceHeaderRepository.save(invoice);

        for (BookingDetail line : lines) {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoiceId(invoice.getInvoiceId());
            // addonId is a plain logical reference — look up the current
            // name fresh rather than trusting anything cached on the line.
            detail.setAddonName(addonRepository.findById(line.getAddonId())
                    .map(Addon::getAddonName).orElse(""));
            detail.setQuantity(line.getQuantity());
            detail.setAddonRate(line.getAddonRate());
            detail.setSubtotal(line.getSubtotal());
            invoiceDetailRepository.save(detail);
        }

        header.setBookingStatus(BookingStatus.COMPLETED);
        bookingHeaderRepository.save(header);

        car.setStatus(CarStatus.AVAILABLE);
        car.setAvailable(true);
        if (request.getFuelStatus() != null) car.setFuelLevel(request.getFuelStatus());
        int extraMiles = request.getExtraMiles() != null ? request.getExtraMiles() : 0;
        car.setOdometer((car.getOdometer() != null ? car.getOdometer() : 0) + extraMiles);
        carRepository.save(car);

        return toDto(invoice);
    }

    private BookingHeader findHeader(String confirmationNo) {
        return bookingHeaderRepository.findByConfirmationNoIgnoreCase(confirmationNo)
                .orElseThrow(() -> new ResourceNotFoundException("No booking found for that confirmation number"));
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        // Every booking, customer info and car (type + registration number)
        // already nested in — the staff booking page's whole reason to
        // exist. Reuses BookingService's own DTO builder (getBookingByConfirmation)
        // rather than duplicating that mapping here, same "one source of
        // truth per entity type" reasoning BookingServiceImpl itself follows.
        return bookingHeaderRepository.findAll().stream()
                .sorted(Comparator.comparing(BookingHeader::getBookingDate).reversed())
                .map(h -> bookingService.getBookingByConfirmation(h.getConfirmationNo()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CarTypeAvailabilityDTO> getDashboard(Long hubId) {
        // The custom aggregate query (CarRepository.countByCarType) does the
        // actual counting in one grouped query rather than pulling every
        // Car row into memory - this is the dashboard "X available / Y
        // handed over / Z in maintenance per car type" screen reads from.
        List<CarRepository.CarTypeCountProjection> counts = carRepository.countByCarType(
                hubId, CarStatus.AVAILABLE, CarStatus.BOOKED, CarStatus.UNDER_MAINTENANCE);
        return counts.stream()
                .map(p -> new CarTypeAvailabilityDTO(
                        p.getCarTypeId(),
                        carTypeRepository.findById(p.getCarTypeId()).map(CarType::getCarTypeName).orElse("Unknown"),
                        p.getTotalCars(), p.getAvailableCars(), p.getBookedCars(), p.getMaintenanceCars()))
                .collect(Collectors.toList());
    }

    private int daysBetween(LocalDateTime pickup, LocalDateTime returnDt) {
        long hours = Duration.between(pickup, returnDt).toHours();
        long fullDays = (long) Math.ceil(hours / 24.0);
        return (int) Math.max(1, fullDays);
    }

    private InvoiceResponseDTO toDto(InvoiceHeader inv) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setInvoiceId(inv.getInvoiceId());
        dto.setInvoiceDate(inv.getInvoiceDate());
        dto.setBookingId(inv.getBookingId());
        dto.setCustomerName(inv.getCustomerName());
        dto.setPhone(inv.getPhone());
        dto.setEmail(inv.getEmail());
        dto.setVehicleNumber(inv.getVehicleNumber());
        dto.setPickupHub(inv.getPickupHub());
        dto.setDropHub(inv.getDropHub());
        dto.setRentalAmount(inv.getRentalAmount());
        dto.setAddonAmount(inv.getAddonAmount());
        dto.setTotalAmount(inv.getTotalAmount());
        dto.setPaymentType(inv.getPaymentType() != null ? inv.getPaymentType().name() : null);
        dto.setPaymentReference(inv.getPaymentReference());
        dto.setPaymentStatus(inv.getPaymentStatus() != null ? inv.getPaymentStatus().name() : null);
        dto.setExtraMiles(inv.getExtraMiles());
        dto.setExtraChargeAmount(inv.getExtraChargeAmount());
        dto.setDamageNotes(inv.getDamageNotes());
        dto.setDays(inv.getDays());
        return dto;
    }
}
